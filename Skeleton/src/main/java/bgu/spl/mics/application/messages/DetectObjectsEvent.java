package bgu.spl.mics.application.messages;

import java.util.List;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.*;;

/**
 * Event representing a request to detect objects.
 */
public class DetectObjectsEvent implements Event<Boolean> {
    private final StampedDetectedObjects detectedObjects;

    public DetectObjectsEvent(StampedDetectedObjects detectedObjects) {
        this.detectedObjects = detectedObjects;
    }

    public StampedDetectedObjects getStampedDetectedObjects() {
        return detectedObjects;
    }

    public List<DetectedObject> getDetectedObjects() {
        return detectedObjects.getDetectedObjects();
    }

    public int getTime() {
        return detectedObjects.getTime();
    }
}
