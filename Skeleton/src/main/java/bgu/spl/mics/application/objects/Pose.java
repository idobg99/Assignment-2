package bgu.spl.mics.application.objects;

/**
 * Represents the robot's pose (position and orientation) in the environment.
 * Includes a CloudPoint for position (x, y) and yaw for orientation.
 */
public class Pose {
    private final CloudPoint position; // Reuses CloudPoint for x, y
    private final float yaw; // Orientation in degrees or radians
    private final int time; // Time the pose was recorded

    /**
     * Constructs a Pose using a CloudPoint for position and a yaw angle.
     *
     * @param time       The timestamp of the pose.
     * @param position   The CloudPoint representing the x, y position.
     * @param yaw        The yaw angle (orientation).
     */
    public Pose(int time, CloudPoint position, float yaw) {
        this.position = position;
        this.yaw = yaw;
        this.time = time;
    }

    /**
     * Constructs a Pose using explicit x, y coordinates and yaw angle.
     *
     * @param time The timestamp of the pose.
     * @param x    The x-coordinate of the pose.
     * @param y    The y-coordinate of the pose.
     * @param yaw  The yaw angle (orientation).
     */
    public Pose(int time, double x, double y, float yaw) {
        this.position = new CloudPoint((int) x, (int) y);
        this.yaw = yaw;
        this.time = time;
    }

    public CloudPoint getPosition() {
        return position;
    }

    public double getX() {
        return position.getX();
    }

    public double getY() {
        return position.getY();
    }

    public float getYaw() {
        return yaw;
    }

    public int getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "Pose{" +
                "position=" + position +
                ", yaw=" + yaw +
                ", time=" + time +
                '}';
    }
}
