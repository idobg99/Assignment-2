package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */
public class Camera {
    private final int id;  // Unique ID for the camera
    private final int frequency;  // Detection frequency
    private STATUS status;  // Status of the camera (e.g., UP or DOWN)
    private final List<DetectedObject> detectedObjectsList;  // List of detected objects

    public Camera(int id, int frequency) {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP;
        this.detectedObjectsList = new ArrayList<>();
    }

    /**
     * Adds detected objects and assigns the timestamp to each object.
     *
     * @param time            The time when objects were detected.
     * @param detectedObjects The list of detected objects.
     */
    public void addDetectedObjects(int time, List<DetectedObject> detectedObjects) { //CHECK IF NECESARY TO MODIFY TIMESTAMP
        for (DetectedObject obj : detectedObjects) {
            obj.setTimestamp(time);
            detectedObjectsList.add(obj);
        }
    }

    /**
     * Retrieves all detected objects for a specific tick.
     *
     * @param time The specific tick to retrieve detected objects for.
     * @return A list of all detected objects at the given tick.
     */
    public List<DetectedObject> getDetectedObjectsAt(int time) {
        List<DetectedObject> result = new ArrayList<>();
        for (DetectedObject obj : detectedObjectsList) {
            if (obj.getTimestamp() == time) {
                result.add(obj);
            }
        }
        return result;
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
                ", detectedObjectsList=" + detectedObjectsList +
                '}';
    }
}
