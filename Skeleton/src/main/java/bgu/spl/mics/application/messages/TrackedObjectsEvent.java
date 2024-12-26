package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.CloudPoint;

import java.util.List;

/**
 * Event representing the tracking of objects detected by LiDAR.
 */
public class TrackedObjectsEvent implements Event<Void> {
    private final int time;                   // Time of detection
    private final String id;                  // Object ID
    private final List<CloudPoint> cloudPoints; // List of points representing the object

    public TrackedObjectsEvent(int time, String id, List<CloudPoint> cloudPoints) {
        this.time = time;
        this.id = id;
        this.cloudPoints = cloudPoints;
    }

    public int getTime() {
        return time;
    }

    public String getId() {
        return id;
    }

    public List<CloudPoint> getCloudPoints() {
        return cloudPoints;
    }

    @Override
    public String toString() {
        return "TrackedObjectsEvent{" +
                "time=" + time +
                ", id='" + id + '\'' +
                ", cloudPoints=" + cloudPoints +
                '}';
    }
}
