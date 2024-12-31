package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.Pose;

/**
 * PoseService is responsible for maintaining the robot's current pose (position and orientation)
 * and broadcasting PoseEvents at every tick.
 */
public class PoseService extends MicroService {

    private final GPSIMU gpsimu;

    /**
     * Constructor for PoseService.
     *
     * @param gpsimu The GPSIMU object that provides the robot's pose data.
     */
    public PoseService(GPSIMU gpsimu) {
        super("PoseService");
        this.gpsimu = gpsimu;
    }

    /**
     * Initializes the PoseService.
     * Subscribes to TickBroadcast and sends PoseEvents at every tick based on the current pose.
     */
    @Override
    protected void initialize() {
        // Subscribe to TickBroadcast
        subscribeBroadcast(TickBroadcast.class, (TickBroadcast tick) -> {
            int currentTick = tick.getTick();

            // Retrieve the pose for the current tick
            Pose currentPose = gpsimu.getPoseAtTick(currentTick);

            if (currentPose != null) {
                // Send a PoseEvent with the current pose
                sendEvent(new PoseEvent(currentPose));
                System.out.println(getName() + " sent PoseEvent: " + currentPose);
            }
        });
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
