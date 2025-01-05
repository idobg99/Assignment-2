package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */
public class Camera {
    private final int id; // Unique ID for the camera
    private final int frequency; // Detection frequency (e.g., how often the camera detects objects)
    private STATUS status; // Status of the camera (e.g., UP or DOWN)
    private final Map<Integer, StampedDetectedObjects> detectedObjectsMap; // Map of detection time to detected objects
    private StampedDetectedObjects lastObjects = null; // Last detected objects

    public static int TOTAL_DETECTED_OBJECTS = 0; // Tracks the total number of detected objects across all cameras

    /**
     * Constructor for the Camera class.
     *
     * @param id           Unique identifier for the camera.
     * @param frequency    Detection frequency.
     * @param detectedData Initial detected data in JSON format.
     */
    public Camera(int id, int frequency, JsonNode detectedData) {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP; // Default status is UP
        this.detectedObjectsMap = parseDetectedObjects(detectedData);
    }

    /**
     * Adds detected objects to the camera's detection map.
     *
     * @param detectedObjects Detected objects with a timestamp.
     */
    public void addDetectedObjects(StampedDetectedObjects detectedObjects) {
        int time = detectedObjects.getTime();
        // Add or replace detected objects for the given time
        detectedObjectsMap.put(time, detectedObjects);
    }

    /**
     * Parses detected objects from JSON data into a map.
     *
     * @param detectedData JSON node containing detection data.
     * @return A map of detection time to detected objects.
     */
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
            // Update the total count of detected objects
            Camera.TOTAL_DETECTED_OBJECTS += objects.size();
        }
        return map;
    }

    /**
     * Retrieves the detected objects available at a specific time.
     *
     * @param currentTime The specific tick to retrieve detected objects for.
     * @return Detected objects at the given time.
     */
    public StampedDetectedObjects getDetectedObjectsAt(int currentTime) {
        this.lastObjects = detectedObjectsMap.get(currentTime);
        return this.lastObjects;
    }

    /**
     * Retrieves the latest detection time available in the map.
     *
     * @return The latest detection time or -1 if the map is empty.
     */
    public int getLastDetectionTime() {
        if (detectedObjectsMap.isEmpty()) {
            return -1; // Default value if no detections are available
        }
        return java.util.Collections.max(detectedObjectsMap.keySet());
    }

    // Getter for camera ID
    public int getId() {
        return id;
    }

    // Getter for camera frequency
    public int getFrequency() {
        return frequency;
    }

    // Getter for camera status
    public STATUS getStatus() {
        return status;
    }

    // Setter for camera status
    public void setStatus(STATUS status) {
        this.status = status;
    }

    // Getter for the last detected objects
    public StampedDetectedObjects GetLastDetectedObjects() {
        return this.lastObjects;
    }

    /**
     * Returns a string representation of the Camera object.
     *
     * @return A string describing the camera.
     */
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
