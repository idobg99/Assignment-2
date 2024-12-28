package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.messages.*;

/**
 * LiDarService is responsible for processing data from the LiDAR sensor and
 * sending TrackedObjectsEvents to the FusionSLAM service.
 */
public class LiDarService extends MicroService {
    private final LiDarWorkerTracker lidarWorker;

    /**
     * Constructor for LiDarService.
     *
     * @param lidarWorker A LiDAR Worker Tracker object that this service will use to process data.
     */
    public LiDarService(LiDarWorkerTracker lidarWorker) {
        super("LiDarService-" + lidarWorker.getId());
        this.lidarWorker = lidarWorker;
    }

    /**
     * Initializes the LiDarService.
     * Registers the service to handle DetectObjectsEvents and TickBroadcasts,
     * and sets up the necessary callbacks for processing data.
     */
    @Override
    protected void initialize() {
        // Subscribe to TickBroadcasts to synchronize with the system clock
        subscribeBroadcast(TickBroadcast.class, (TickBroadcast tick) -> {
            int currentTime = tick.getTick();
            System.out.println(getName() + " received TickBroadcast: " + currentTime);

            // Process any pending LiDAR operations if necessary
            processLidarData(currentTime);
        });

        // Subscribe to DetectObjectsEvent
        subscribeEvent(DetectObjectsEvent.class, (DetectObjectsEvent event) -> {
            System.out.println(getName() + " received DetectObjectsEvent: " + event);

            // Process the detection event and generate a response
            TrackedObjectsEvent trackedObjects = lidarWorker.processDetectionEvent(event);

            // Send the tracked objects to FusionSLAM or another service
            sendEvent(trackedObjects);
            System.out.println(getName() + " sent TrackedObjectsEvent: " + trackedObjects);
        });

        System.out.println(getName() + " initialized.");
    }

    /**
     * Processes any LiDAR data at the given tick.
     * This method is a placeholder for future logic.
     *
     * @param currentTime The current system tick.
     */
    private void processLidarData(int currentTime) {
        // Placeholder: Add logic to process data from LiDAR at this time
        System.out.println(getName() + " is processing LiDAR data for time: " + currentTime);
    }
}
