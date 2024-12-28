package bgu.spl.mics.application.services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
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
    private StatisticalFolder statfolder = StatisticalFolder.getInstance();
    //private int currentTime;

    // List of pending tracked events with countdowns
    private final List<Object[]> pendingTrackedEvents; // Each entry: {TrackedObjectsEvent, countdown}

    /**
     * Constructor for LiDarService.
     *
     * @param lidarWorker A LiDAR Worker Tracker object that this service will use to process data.
     */
    public LiDarService(LiDarWorkerTracker lidarWorker) {
        super("LiDarService-" + lidarWorker.getId());
        this.lidarWorker = lidarWorker;
        //this.currentTime = 0;
        this.pendingTrackedEvents = new ArrayList<>();
    }

    /**
     * Initializes the LiDarService.
     */
    @Override
    protected void initialize() {
        // Handle TickBroadcast to update time and process pending tracked events
        subscribeBroadcast(TickBroadcast.class, tick -> {
            //currentTime = tick.getTick();

            // Process pending tracked events
            Iterator<Object[]> iterator = pendingTrackedEvents.iterator();
            while (iterator.hasNext()) {
                Object[] entry = iterator.next();
                TrackedObjectsEvent trackedEvent = (TrackedObjectsEvent) entry[0];
                int remainingTicks = (int) entry[1];

                // Decrement countdown
                remainingTicks--;

                if (remainingTicks <= 0) {
                    // Send the tracked objects event when countdown reaches zero
                    sendEvent(trackedEvent);

                    // Update statistics
                    statfolder.incrementDetectedObjects(((TrackedObjectsEvent)entry[0]).getTrackedObjects().size());
                    System.out.println(getName() + " sent TrackedObjectsEvent: " + trackedEvent);

                    // Remove the processed event from the list
                    iterator.remove();
                } else {
                    // Update the countdown
                    entry[1] = remainingTicks;
                }
            }
        });

        // Handle DetectObjectsEvent
        subscribeEvent(DetectObjectsEvent.class, event -> {
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
                }
            }

            // Create a TrackedObjectsEvent and add it to pending tracked events
            //WHAT IS THE TIME NEEDED? THE DETECTION TIME OR THE TRACKING TIME
            TrackedObjectsEvent trackedEvent = new TrackedObjectsEvent(event.getTime(), trackedObjects); 
            if (lidarWorker.getFrequency() == 0){
                sendEvent(trackedEvent);

                // Update statistics
                statfolder.incrementDetectedObjects(trackedEvent.getTrackedObjects().size());
                System.out.println(getName() + " sent TrackedObjectsEvent: " + trackedEvent);
            }
            else {
                pendingTrackedEvents.add(new Object[]{trackedEvent, lidarWorker.getFrequency()});
                //System.out.println(getName() + " queued TrackedObjectsEvent: " + trackedEvent);
            }
        });

        System.out.println(getName() + " initialized.");
    }
}
