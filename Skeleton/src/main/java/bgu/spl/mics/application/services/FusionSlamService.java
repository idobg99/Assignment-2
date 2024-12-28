package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.TrackedObject;
import bgu.spl.mics.application.objects.LandMark;

import java.util.List;

/**
 * FusionSlamService integrates data from multiple sensors to build and update
 * the robot's global map.
 * 
 * This service receives TrackedObjectsEvents from LiDAR workers and PoseEvents from the PoseService,
 * transforming and updating the map with new landmarks.
 */
public class FusionSlamService extends MicroService {
    private final FusionSlam fusionSlam;
    private Pose currentPose = null;

    /**
     * Constructor for FusionSlamService.
     *
     * @param fusionSlam The FusionSLAM object responsible for managing the global map.
     */
    public FusionSlamService(FusionSlam fusionSlam) {
        super("FusionSlamService");
        this.fusionSlam = fusionSlam;
        
    }

    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle TrackedObjectsEvents, PoseEvents, and broadcasts,
     * and sets up callbacks for updating the global map.
     */
    @Override
    protected void initialize() {
        // Handle PoseEvent
        subscribeEvent(PoseEvent.class, poseEvent -> {
            currentPose = poseEvent.getPose();
            fusionSlam.addPose(currentPose);
            System.out.println(getName() + " updated pose: " + currentPose);
            complete(poseEvent, null);
        });

        // Handle TrackedObjectsEvent
        subscribeEvent(TrackedObjectsEvent.class, trackedObjectsEvent -> {
            List<TrackedObject> trackedObjects = trackedObjectsEvent.getTrackedObjects();

            for (TrackedObject trackedObject : trackedObjects) {
                // INSERT CALCULATION FOR GLOBAL MAP//////////////////
                LandMark newLandmark = new LandMark(trackedObject.getId(),
                                                    trackedObject.getDescription(),
                                                    trackedObject.getCoordinates());
                fusionSlam.insertLandmark(newLandmark);
                System.out.println(getName() + " added landmark: " + newLandmark);
            }
        });

        // Handle TickBroadcast
        subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
            // Perform periodic updates or maintenance tasks
            System.out.println(getName() + " received tick: " + tickBroadcast.getTick());
        });

        // Handle TerminatedBroadcast
        subscribeBroadcast(TerminatedBroadcast.class, terminatedBroadcast -> {
            System.out.println(getName() + " received termination signal. Shutting down.");
            terminate();
        });

        // Handle CrashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast -> {
            System.err.println(getName() + " received crash notification: " + crashedBroadcast.getReason());
            // Perform any cleanup or map adjustment due to crash
        });

        System.out.println(getName() + " initialized.");
    }
}
