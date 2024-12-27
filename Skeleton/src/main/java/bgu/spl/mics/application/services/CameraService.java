package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.DetectedObject;

import java.util.List;

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

            // Process detections only if the tick matches the camera's frequency
            if (currentTick % camera.getFrequency() == 0 && currentTick > lastProcessedTick) { //CHECK IF MATCHES TO THE FREQUENCY TIME REQUEST OF THE ASSIGNMENT
                List<DetectedObject> detectedObjects = camera.getDetectedObjectsAt(currentTick);
                lastProcessedTick = currentTick;

                // Send a DetectObjectsEvent for each detected object
                for (DetectedObject obj : detectedObjects) {
                    sendEvent(new DetectObjectsEvent(obj.getId()));
                    System.out.println(getName() + " sent DetectObjectsEvent for " + obj);
                }
            }
        });

        System.out.println(getName() + " initialized.");
    }
}
