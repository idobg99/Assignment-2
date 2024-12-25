package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;

/**
 * Event representing the robot's pose (coordinates and orientation).
 * Sent by PoseService, handled by Fusion-SLAM.
 */
public class PoseEvent implements Event<Void> {
    private final double x;
    private final double y;
    private final double theta; // Orientation in degrees

    public PoseEvent(double x, double y, double theta) {
        this.x = x;
        this.y = y;
        this.theta = theta;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getTheta() {
        return theta;
    }

    @Override
    public String toString() {
        return "PoseEvent{" + "x=" + x + ", y=" + y + ", theta=" + theta + '}';
    }
}
