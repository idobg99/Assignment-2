package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
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
     * Registers the service to handle TrackedObjectsEvents, PoseEvents, and TickBroadcasts,
     * and sets up callbacks for updating the global map.
     */
    @Override
    protected void initialize() {
        // Handle PoseEvent
        subscribeEvent(PoseEvent.class, poseEvent -> {
            Pose currentPose = poseEvent.getPose();
            fusionSlam.addPose(currentPose);
            System.out.println(getName() + " updated pose: " + currentPose);
        });

        // Handle TrackedObjectsEvent
        subscribeEvent(TrackedObjectsEvent.class, trackedObjectsEvent -> {
            List<TrackedObject> trackedObjects = trackedObjectsEvent.getTrackedObjects();

            for (TrackedObject trackedObject : trackedObjects) {
                //CHECK FOR ALREADY DETECTED LANDMARKS
                LandMark newLandmark = new LandMark(trackedObject.getId(),
                                                    trackedObject.getDescription(),
                                                    trackedObject.getCoordinates());
                int landmarkIndex = trackedObject.getId().hashCode() % 1000; // Example hashing logic for index
                fusionSlam.insertLandmark(landmarkIndex, newLandmark);
                System.out.println(getName() + " added landmark: " + newLandmark);
            }
        });

        // Handle TickBroadcast
        subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
            // Update system runtime in the StatisticalFolder or other periodic tasks
            System.out.println(getName() + " received tick: " + tickBroadcast.getTick());
        });

        System.out.println(getName() + " initialized.");
    }
}
