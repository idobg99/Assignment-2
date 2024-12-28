package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;

/**
 * Event representing a request to detect objects.
 */
public class DetectObjectsEvent implements Event<String> {
    private final String objectId;
    private final String description;

    public DetectObjectsEvent(String objectId, String description) {
        this.objectId = objectId;
        this.description = description;
    }

    public String getObjectId() {
        return objectId;
    }

    public String getObjDescription() {
        return description;
    }
}
