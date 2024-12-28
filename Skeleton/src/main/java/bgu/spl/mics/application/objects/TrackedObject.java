package bgu.spl.mics.application.objects;

//import java.util.*;


/**
 * Represents an object tracked by the LiDAR.
 * This object includes information about the tracked object's ID, description, 
 * time of tracking, and coordinates in the environment.
 */
public class TrackedObject {
    private final String id;
    //private final int time;
    private final String description;
    //private List<CloudPoint> coordinates;
    private StampedCloudPoints coordinates;

    public TrackedObject(String id, String description, StampedCloudPoints coordinates) {
        this.id = id;
        //this.time = time;
        this.description = description;
        this.coordinates = coordinates;
        //this.coordinates = new ArrayList<>(coordinates);

    }

    public String getId() {
        return id;
    }

    public int getTime() {
        return coordinates.getTime();
    }

    public String getDescription() {
        return description;
    }

    public StampedCloudPoints getCoordinates() {
        return coordinates;
    }

    @Override
    public String toString() {
        return "TrackedObject{id='" + id + "', time=" + coordinates.getTime() + ", description='" + description + "', coordinates=" + coordinates + "}";
    }
}
