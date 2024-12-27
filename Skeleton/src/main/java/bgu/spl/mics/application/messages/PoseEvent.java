package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Pose;

/**
 * Event representing the robot's pose (position and orientation).
 */
public class PoseEvent implements Event<Void> {
    private final Pose pose; // Robot's pose including position, orientation, and time

    /**
     * Constructs a PoseEvent with a given Pose object.
     *
     * @param pose The Pose object representing the robot's position and orientation.
     */
    public PoseEvent(Pose pose) {
        this.pose = pose;
    }

    public Pose getPose() {
        return pose;
    }

    public int getTime() {
        return pose.getTime();
    }

    public double getX() {
        return pose.getX();
    }

    public double getY() {
        return pose.getY();
    }

    public float getYaw() {
        return pose.getYaw();
    }

    @Override
    public String toString() {
        return "PoseEvent{" +
                "pose=" + pose +
                '}';
    }
}
