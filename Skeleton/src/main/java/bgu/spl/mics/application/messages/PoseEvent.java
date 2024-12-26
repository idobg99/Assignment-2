package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.CloudPoint;

/**
 * Event representing the robot's pose (position and orientation).
 */
public class PoseEvent implements Event<Void> {
    private final int time;      // Time of the pose
    private final CloudPoint position; // Position of the robot
    private final double theta; // Orientation in degrees

    public PoseEvent(int time, CloudPoint position, double theta) {
        this.time = time;
        this.position = position;
        this.theta = theta;
    }

    public int getTime() {
        return time;
    }

    public CloudPoint getPosition() {
        return position;
    }

    public double getTheta() {
        return theta;
    }

    @Override
    public String toString() {
        return "PoseEvent{" +
                "time=" + time +
                ", position=" + position +
                ", theta=" + theta +
                '}';
    }
}
