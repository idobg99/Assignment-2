package bgu.spl.mics.application.objects;

//import java.util.ArrayList;
import java.util.HashMap;
//import java.util.List;
import java.util.Map;

/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */
public class Camera {
    private final int id;  // Unique ID for the camera
    private final int frequency;  // Detection frequency
    private STATUS status;  // Status of the camera (e.g., UP or DOWN)
    private final  Map<Integer,StampedDetectedObjects> detectedObjectsMap;  // List of detected objects with availability times

    public Camera(int id, int frequency) {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP;
        this.detectedObjectsMap = new HashMap<>();
    }

    /**
     * Adds detected objects and calculates their availability time.
     * Assumes input is StampedDetectedObjects built from the json.
     * input is in batches - StampedDetectedObjects each time
     *
     * @param detectionTime   The time when the objects were detected.
     * @param detectedObjects The list of detected objects.
     */
    public void addDetectedObjects(StampedDetectedObjects detectedObjects) {
        int time = detectedObjects.getTime();

        // Add or replace the StampedDetectedObjects for the given time
        detectedObjectsMap.put(time, detectedObjects);
    }

    /**
     * Retrieves all detected objects available at a specific tick.
     *
     * @param currentTime The specific tick to retrieve detected objects for.
     * @return A list of all detected objects available at the given tick.
     */
    public StampedDetectedObjects getDetectedObjectsAt(int currentTime) {
        return detectedObjectsMap.get(currentTime);
    }

    public int getId() {
        return id;
    }

    public int getFrequency() {
        return frequency;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Camera{" +
                "id=" + id +
                ", frequency=" + frequency +
                ", status=" + status +
                ", detectedObjectsList=" + detectedObjectsMap +
                '}';
    }
}