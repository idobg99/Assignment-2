package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;

/**
 * Event representing a request to detect objects.
 */
public class DetectObjectsEvent implements Event<String> {
    private final String objectId;

    public DetectObjectsEvent(String objectId) {
        this.objectId = objectId;
    }

    public String getObjectId() {
        return objectId;
    }
}
