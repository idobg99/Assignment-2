package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

//import java.util.List;

/**
 * CameraService processes data from the camera and
 * sends DetectObjectsEvent messages to other services.
 */
public class CameraService extends MicroService {
    private final Camera camera;
    private int lastProcessedTick; // Tracks the last tick this service processed

    public CameraService(Camera camera) {
        super("CameraService-" + camera.getId());
        this.camera = camera;
        this.lastProcessedTick = 0;
    }

    @Override
    protected void initialize() {
        // Subscribe to TickBroadcast
        subscribeBroadcast(TickBroadcast.class, (TickBroadcast tick) -> {
            int currentTick = tick.getTick();

            // Ensure the service processes each tick only once
            if (currentTick > lastProcessedTick) {
                StampedDetectedObjects detectedObjects = camera.getDetectedObjectsAt(currentTick);

                // Process detected objects and send events
                for (DetectedObject obj : detectedObjects.getDetectedObjects()) {
                    sendEvent(new DetectObjectsEvent(obj.getId(),obj.getDescription()));
                    System.out.println(getName() + " sent DetectObjectsEvent for " + obj);
                }

                // Update last processed tick
                lastProcessedTick = currentTick;
            }
        });

        System.out.println(getName() + " initialized.");
    }

    /**
     * Retrieves the last processed tick for this service.
     *
     * @return The last processed tick.
     */
    public int getLastProcessedTick() {
        return lastProcessedTick;
    }
}
