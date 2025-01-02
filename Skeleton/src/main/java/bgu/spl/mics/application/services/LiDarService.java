package bgu.spl.mics.application.services;

import java.util.LinkedList;
import java.util.Queue;
import java.util.ArrayList;
import java.util.List;

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

    // Queue to hold pending detection events
    private final Queue<DetectObjectsEvent> pendingTrackedEvents;

    /**
     * Constructor for LiDarService.
     *
     * @param lidarWorker A LiDAR Worker Tracker object that this service will use to process data.
     */
    public LiDarService(LiDarWorkerTracker lidarWorker) {
        super("LiDarService-" + lidarWorker.getId());
        this.lidarWorker = lidarWorker;
        this.pendingTrackedEvents = new LinkedList<>();
    }

    /**
     * Initializes the LiDarService.
     */
    @Override
    protected void initialize() {
        // Handle TickBroadcast to process pending tracked events
        subscribeBroadcast(TickBroadcast.class, tick -> {
            int currentTick = tick.getTick();

            // Process pending tracked events
            while (!pendingTrackedEvents.isEmpty()) {
                DetectObjectsEvent event = pendingTrackedEvents.peek();
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
                            System.err.println(getName() + " failed to track object: " + obj.getId());
                            complete(event, false);
                            sendBroadcast(new CrashedBroadcast(lidarWorker.getId() + " found error in data"));
                        }
                    }

                    // Create and send TrackedObjectsEvent
                    TrackedObjectsEvent trackedEvent = new TrackedObjectsEvent(event.getTime(), trackedObjects);
                    sendEvent(trackedEvent);
                    complete(event, true);
                    System.out.println(getName() + " sent TrackedObjectsEvent: " + trackedEvent);

                    // Update statistics
                    //System.out.println("SIZE OF THE DET OBJ ################# - " + trackedObjects.size());
                    statfolder.incrementTrackedObjects(trackedObjects.size());
                    //System.out.println(getName() + " sent TrackedObjectsEvent: " + trackedEvent);

                    // Remove the processed event from the queue
                    pendingTrackedEvents.poll();
                } else {
                    break; // The next event is not ready yet
                }
            }
        });

        // Handle DetectObjectsEvent
        subscribeEvent(DetectObjectsEvent.class, event -> {
            /*if (lidarWorker.getFrequency() == 0) {
                StampedDetectedObjects detectedObjects = event.getStampedDetectedObjects();

                //System.out.println("LIDAR THE MAN@@@@@@ - ");

                List<TrackedObject> trackedObjects = new ArrayList<>();
                for (DetectedObject obj : detectedObjects.getDetectedObjects()) {

                    //System.out.println("THE LIDAR BOY - " + obj.getId() + " time - "  + detectedObjects.getTime());

                    TrackedObject trackedObject = lidarWorker.trackObject(
                            detectedObjects.getTime(),
                            obj.getId(),
                            obj.getDescription()
                    );

                    if (trackedObject != null) {
                        trackedObjects.add(trackedObject);
                    } else {
                        System.out.println(getName() + " FAILED to track object: " + obj.getId());
                        complete(event, false);
                        sendBroadcast(new CrashedBroadcast(lidarWorker.getId() + " found error in data"));
                    }
                }

                TrackedObjectsEvent trackedEvent = new TrackedObjectsEvent(event.getTime(), trackedObjects);

                // Send event
                sendEvent(trackedEvent);
                complete(event, true);

                // Update statistics
                statfolder.incrementTrackedObjects(trackedObjects.size());
                System.out.println(getName() + " sent TrackedObjectsEvent: " + trackedEvent);
            } else {*/
            pendingTrackedEvents.offer(event);
            //}
        });
        // Handle TerminatedBroadcast
        subscribeBroadcast(TerminatedBroadcast.class, terminatedBroadcast -> {
            System.out.println(getName() + " received termination signal. Shutting down.");
            terminate();
        });

        // Handle CrashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast -> {
            System.err.println(getName() + " received crash notification: " + crashedBroadcast.getReason());
            System.err.println(getName() + " checkn: " + crashedBroadcast.getReason());
            statfolder.addLastTrackedObject(lidarWorker.getLastTrackedObjects());
            
            terminate();
            // Perform any cleanup or map adjustment due to crash
        });

        System.out.println(getName() + " initialized.");
    }
}
