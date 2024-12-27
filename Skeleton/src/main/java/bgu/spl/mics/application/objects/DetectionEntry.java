package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * Represents a detection entry with a timestamp and a list of detected objects.
 */
public class DetectionEntry {
    private final int time;
    private final List<String> detectedObjects;

    public DetectionEntry(int time, List<String> detectedObjects) {
        this.time = time;
        this.detectedObjects = detectedObjects;
    }

    public int getTime() {
        return time;
    }

    public List<String> getDetectedObjects() {
        return detectedObjects;
    }
}
