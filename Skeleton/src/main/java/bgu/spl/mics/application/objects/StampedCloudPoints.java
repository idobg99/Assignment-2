package bgu.spl.mics.application.objects;

import java.util.*;


/**
 * Represents a group of cloud points corresponding to a specific timestamp.
 * Used by the LiDAR system to store and process point cloud data for tracked objects.
 */
public class StampedCloudPoints {
    private final String id;
    private final int time;
    private List<List<Double>> cloudPoints;

    public StampedCloudPoints(String id, int time, List<List<Double>> cloudPoints) {
        this.id = id;
        this.time = time;
        this.cloudPoints = new ArrayList<>(cloudPoints);
    }

    public String getId() {
        return id;
    }

    public int getTime() {
        return time;
    }

    public List<List<Double>> getCloudPoints() {
        return Collections.unmodifiableList(cloudPoints);
    }

    @Override
    public String toString() {
        return "StampedCloudPoints{id='" + id + "', time=" + time + ", cloudPoints=" + cloudPoints + "}";
    }
}