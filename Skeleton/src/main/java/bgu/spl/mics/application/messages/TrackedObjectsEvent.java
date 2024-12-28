package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
//import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.TrackedObject;

import java.util.List;

/**
 * Event representing the tracking of objects detected by LiDAR.
 */
public class TrackedObjectsEvent implements Event<Void> {
    private final int time;                   // Time of detection 
    private final List<TrackedObject> trackedObjects; // List of tracked objects

    public TrackedObjectsEvent(int time, List<TrackedObject> trackedObjects) {
        this.time = time;
        this.trackedObjects = trackedObjects;
    }

    public int getTime() {
        return time;
    }

    public List<TrackedObject> getTrackedObjects() {
        return trackedObjects;
    }

    @Override
    public String toString() {
        return "TrackedObjectsEvent{" +
                "time=" + time +
                ", trackedObjects='" + trackedObjects +
                '}';
    }
}
