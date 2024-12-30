package bgu.spl.mics.application.objects;

import java.util.ArrayList;
//import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
//import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */
public class Camera {
    private final int id;  // Unique ID for the camera
    private final int frequency;  // Detection frequency
    private STATUS status;  // Status of the camera (e.g., UP or DOWN)
    private final  Map<Integer,StampedDetectedObjects> detectedObjectsMap;  // List of detected objects with availability times
    private StampedDetectedObjects lastObjects = null;

    public Camera(int id, int frequency,JsonNode detectedData ) {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP;
        this.detectedObjectsMap = parseDetectedObjects(detectedData);
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

    public Map<Integer, StampedDetectedObjects> parseDetectedObjects(JsonNode detectedData) {
        Map<Integer, StampedDetectedObjects> map = new HashMap<>();
        for (JsonNode entry : detectedData) {
            int time = entry.get("time").asInt();
            List<DetectedObject> objects = new ArrayList<>();
            for (JsonNode obj : entry.get("detectedObjects")) {
                String id = obj.get("id").asText();
                String description = obj.get("description").asText();
                objects.add(new DetectedObject(id, description));
            }
            map.put(time, new StampedDetectedObjects(time, objects));
        }
        return map;
    }

    /**
     * Retrieves all detected objects available at a specific tick.
     *
     * @param currentTime The specific tick to retrieve detected objects for.
     * @return A list of all detected objects available at the given tick.
     */
    public StampedDetectedObjects getDetectedObjectsAt(int currentTime) {
        this.lastObjects = detectedObjectsMap.get(currentTime);
        return this.lastObjects;
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

    public StampedDetectedObjects GetLastDetectedObjects() {
        return this.lastObjects;
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