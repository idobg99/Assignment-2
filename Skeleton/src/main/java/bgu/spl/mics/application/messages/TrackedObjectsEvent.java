package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
//import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.StampedCloudPoints;
import bgu.spl.mics.application.objects.TrackedObject;

//import java.util.List;

/**
 * Event representing the tracking of objects detected by LiDAR.
 */
public class TrackedObjectsEvent implements Event<Void> {
    private final TrackedObject object;

    public TrackedObjectsEvent(TrackedObject object) {
        this.object = object;
    }

    public int getTime() {
        return object.getTime();
    }

    public String getId() {
        return object.getId();
    }

    public String getDescription() {
        return object.getDescription();
    }

    public StampedCloudPoints getCloudPoints() {
        return object.getCoordinates();
    }

    @Override
    public String toString() {
        return "TrackedObjectsEvent{" +
                "time=" + object.getTime() +
                ", id='" + object.getId() + '\'' +
                ", cloudPoints=" + object.getCoordinates() +
                '}';
    }
}
