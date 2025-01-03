package bgu.spl.mics.application.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.objects.TrackedObject;

/**
 * LiDarService processes detected objects and sends TrackedObjectsEvents when the time is right.
 */
public class LiDarService extends MicroService {
    private final LiDarWorkerTracker lidarWorker;
    private final StatisticalFolder statfolder = StatisticalFolder.getInstance();
    private int lastProcessedTick; // Tracks the last tick processed

    // List to hold pending DetectObjectsEvents
    private final List<DetectObjectsEvent> pendingTrackedEvents;

    /**
     * Constructor for LiDarService.
     *
     * @param lidarWorker A LiDAR Worker Tracker object that this service will use to process data.
     */
    public LiDarService(LiDarWorkerTracker lidarWorker) {
        super("LiDarService-" + lidarWorker.getId());
        this.lidarWorker = lidarWorker;
        this.pendingTrackedEvents = new ArrayList<>();
        this.lastProcessedTick = 0; // Initialize at tick 0
    }

    /**
     * Initializes the LiDarService.
     */
    @Override
    protected void initialize() {
        // Handle TickBroadcast to process events in batches
        subscribeBroadcast(TickBroadcast.class, tick -> {
            int currentTick = tick.getTick();

            // Process events only if we reach the next frequency tick
            if ((currentTick - lastProcessedTick) >= lidarWorker.getFrequency()) {
                // Map to group tracked objects by their detection time
                Map<Integer, List<TrackedObject>> trackedObjectsByTime = new HashMap<>();

                // List to track processed events
                List<DetectObjectsEvent> processedEvents = new ArrayList<>();

                for (DetectObjectsEvent event : pendingTrackedEvents) {
                    int detectionTime = event.getTime();

                    // Check if the event is ready to be processed
                    if (currentTick - lidarWorker.getFrequency() >= detectionTime) {
                        StampedDetectedObjects detectedObjects = event.getStampedDetectedObjects();

                        List<TrackedObject> trackedObjects = new ArrayList<>();
                        for (DetectedObject obj : detectedObjects.getDetectedObjects()) {
                            TrackedObject trackedObject = lidarWorker.trackObject(
                                    detectedObjects.getTime(),
                                    obj.getId(),
                                    obj.getDescription()
                            );

                            if (trackedObject != null) {
                                trackedObjects.add(trackedObject);
                            } else {
                                System.out.println(getName() + " failed to track object: " + obj.getId());
                                complete(event, false);
                                sendBroadcast(new CrashedBroadcast("LiDAR-" + lidarWorker.getId() + " found error in data"));
                                terminate(); // Shut down on error
                                return;
                            }
                        }

                        // Group tracked objects by detection time
                        trackedObjectsByTime
                                .computeIfAbsent(detectionTime, k -> new ArrayList<>())
                                .addAll(trackedObjects);

                        // Mark the event as processed
                        complete(event, true);
                        processedEvents.add(event);
                    }
                }

                // Remove processed events from the pending list
                pendingTrackedEvents.removeAll(processedEvents);

                // Send separate TrackedObjectsEvents for each detection time
                for (Map.Entry<Integer, List<TrackedObject>> entry : trackedObjectsByTime.entrySet()) {
                    int detectionTime = entry.getKey();
                    List<TrackedObject> trackedObjects = entry.getValue();

                    TrackedObjectsEvent trackedEvent = new TrackedObjectsEvent(detectionTime, trackedObjects);
                    sendEvent(trackedEvent);

                    // Update statistics
                    statfolder.incrementTrackedObjects(trackedObjects.size());
                    System.out.println(getName() + " sent TrackedObjectsEvent for time " + detectionTime + ": "/*+ trackedEvent */ );
                }

                // Update the last processed tick
                lastProcessedTick = currentTick;
            }
        });

        // Handle DetectObjectsEvent
        subscribeEvent(DetectObjectsEvent.class, event -> {
            // Add the event to the pending list
            pendingTrackedEvents.add(event);
        });

        // Handle TerminatedBroadcast
        subscribeBroadcast(TerminatedBroadcast.class, terminatedBroadcast -> {
            System.out.println(getName() + " received termination signal. Shutting down.");
            terminate();
        });

        // Handle CrashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast -> {
            System.err.println(getName() + " received crash notification: " + crashedBroadcast.getReason());
            statfolder.addLastTrackedObject(lidarWorker.getLastTrackedObjects());
            terminate();
        });

        System.out.println(getName() + " initialized.");
    }
}
