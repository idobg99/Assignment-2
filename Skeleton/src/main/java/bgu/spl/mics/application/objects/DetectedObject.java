package bgu.spl.mics.application.objects;

//import java.util.List;

/**
 * Represents an object detected by a camera.
 */
public class DetectedObject {
    private final String id;
    private final String description;

    public DetectedObject(String id, String description) {
        this.id = id;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "DetectedObject{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' + '}';
    }
}