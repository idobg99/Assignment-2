package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.Camera;
//import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * CameraService processes data from the camera and
 * sends DetectObjectsEvent messages to other services.
 */
public class CameraService extends MicroService {
    private final Camera camera;
    private int lastProcessedTick; // Tracks the last tick this service processed

    // List to hold pending events with countdowns
    private final List<Object[]> pendingEvents; // Each entry: {DetectObjectsEvent, countdown}

    public CameraService(Camera camera) {
        super("CameraService-" + camera.getId());
        this.camera = camera;
        this.lastProcessedTick = 0;
        this.pendingEvents = new ArrayList<>();
    }

    @Override
    protected void initialize() {
        // Subscribe to TickBroadcast
        subscribeBroadcast(TickBroadcast.class, (TickBroadcast tick) -> {
            int currentTick = tick.getTick();

            // Process pending events
            Iterator<Object[]> iterator = pendingEvents.iterator();
            while (iterator.hasNext()) {
                Object[] entry = iterator.next();
                //StampedDetectedObjects detectedObjects = (StampedDetectedObjects) entry[0];
                int remainingTicks = (int) entry[1];

                // Decrement countdown
                remainingTicks--;

                if (remainingTicks <= 0) {
                    // Countdown reached 0; send events for the detected objects
                    
                    sendEvent((DetectObjectsEvent)entry[0]);
                    
                    iterator.remove(); // Remove the event from the list
                } else {
                    // Update countdown
                    entry[1] = remainingTicks;
                }
            }

            // Ensure the service processes new detections only once per tick
            if (currentTick > lastProcessedTick) {
                StampedDetectedObjects detectedObjects = camera.getDetectedObjectsAt(currentTick);

                if (detectedObjects != null) {
                    // Add new detections to the list with their delay
                    DetectObjectsEvent e = new DetectObjectsEvent(detectedObjects);
                    pendingEvents.add(new Object[]{e, camera.getFrequency()});
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
