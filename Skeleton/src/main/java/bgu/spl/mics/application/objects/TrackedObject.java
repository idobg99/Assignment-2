package bgu.spl.mics.application.objects;

import java.util.*;


/**
 * Represents an object tracked by the LiDAR.
 * This object includes information about the tracked object's ID, description, 
 * time of tracking, and coordinates in the environment.
 */
public class TrackedObject {
    
    private final String description; // ??
    private final StampedCloudPoints object;
    
    public TrackedObject(StampedCloudPoints object, String description) {
        this.description = description;
        this.object = object;
    }

    public String getId() {
        return object.getId();
    }

    public int getTime() {
        return object.getTime();
    }

    public String getDescription() {
        return description;
    }

    public List<CloudPoint> getCoordinates() {
        return Collections.unmodifiableList(object.getCloudPoints());
    }

    @Override
    public String toString() {
        return "TrackedObject{object(StampedCloudPoints)='" +object + ", description='" + description + "}";
    }
}
