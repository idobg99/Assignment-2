package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.TrackedObject;
import bgu.spl.mics.application.objects.LandMark;

import java.util.ArrayList;
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
     * Registers the service to handle TrackedObjectsEvents, PoseEvents, and broadcasts,
     * and sets up callbacks for updating the global map.
     */
    @Override
    protected void initialize() {
        // Handle PoseEvent
        subscribeEvent(PoseEvent.class, poseEvent -> {
            Pose currentPose = poseEvent.getPose();
            fusionSlam.addPose(currentPose);
            System.out.println(getName() + " updated pose: " + currentPose);
            complete(poseEvent, null);
        });

        // Handle TrackedObjectsEvent
        subscribeEvent(TrackedObjectsEvent.class, trackedObjectsEvent -> {
            List<TrackedObject> trackedObjects = trackedObjectsEvent.getTrackedObjects();
            Pose currentPose = fusionSlam.getPoseAt(trackedObjectsEvent.getTime());

            //System.out.println("--------Time: " + trackedObjectsEvent.getTime() + ", Pose: " + currentPose);

            if (currentPose == null || trackedObjects == null || trackedObjects.isEmpty()) {
                System.out.println(getName() + " cannot process tracked objects: no current pose or objects available.");
                complete(trackedObjectsEvent, null);
                return;
            }

            for (TrackedObject trackedObject : trackedObjects) {
                // Calculating the global coordinates
                List<CloudPoint> coordinates = new ArrayList<>();

                for (CloudPoint tobj : trackedObject.getCoordinates()) {
                    //System.out.println("THIS ONE - x: " + tobj.getX() + " Y: " + tobj.getY());
                    double xGlobal = currentPose.getX() + (tobj.getX() * Math.cos(Math.toRadians(currentPose.getYaw())) -
                                                           tobj.getY() * Math.sin(Math.toRadians(currentPose.getYaw())));
                    double yGlobal = currentPose.getY() + (tobj.getX() * Math.sin(Math.toRadians(currentPose.getYaw())) +
                                                           tobj.getY() * Math.cos(Math.toRadians(currentPose.getYaw())));
                    coordinates.add(new CloudPoint(xGlobal, yGlobal));

                    //System.out.println("Object: " + trackedObject.getId() + " Local: (" + tobj.getX() + ", " + tobj.getY() + "), Pose: (" + currentPose.getX() + ", " + currentPose.getY() + ", " + currentPose.getYaw() + "), Global: (" + xGlobal + ", " + yGlobal + ")");
                }

                LandMark newLandmark = new LandMark(trackedObject.getId(),
                                                    trackedObject.getDescription(),
                                                    coordinates);
                fusionSlam.insertLandmark(newLandmark);
                System.out.println(getName() + " added landmark: " + newLandmark);
            }

            // Complete the event
            complete(trackedObjectsEvent, null);

            /*// Terminate simulatoin if detectionTime=lastTime
            if (fusionSlam.getLastDetectionTime() == trackedObjectsEvent.getTime()) {

                System.out.print("FINITO**************************** " + trackedObjectsEvent.getTime());
                sendBroadcast(new TerminatedBroadcast());
            }*/
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
            terminate();
            // Perform any cleanup or map adjustment due to crash
        });

        System.out.println(getName() + " initialized.");
    }

    
}
