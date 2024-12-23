package bgu.spl.mics.application.objects;
import java.util.*;


/**
 * Represents objects detected by the camera at a specific timestamp.
 * Includes the time of detection and a list of detected objects.
 */
public class StampedDetectedObjects {
    private final int time;
    private List<DetectedObject> detectedObjects;

    public StampedDetectedObjects(int time, List<DetectedObject> detectedObjects) {
        this.time = time;
        this.detectedObjects = new ArrayList<>(detectedObjects);
    }

    public int getTime() {
        return time;
    }

    public List<DetectedObject> getDetectedObjects() {
        return Collections.unmodifiableList(detectedObjects);
    }

    @Override
    public String toString() {
        return "StampedDetectedObjects{time=" + time + ", detectedObjects=" + detectedObjects + "}";
    }
}