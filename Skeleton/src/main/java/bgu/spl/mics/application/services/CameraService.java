package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.StatisticalFolder;

import java.util.LinkedList;
import java.util.Queue;

/**
 * CameraService processes data from the camera and
 * sends DetectObjectsEvent messages to other services.
 */
public class CameraService extends MicroService {
    private final Camera camera;
    private int lastProcessedTick; // Tracks the last tick this service processed
    private StatisticalFolder statfolder = StatisticalFolder.getInstance();

    // Queue to hold pending events with their detection times
    private final Queue<DetectObjectsEvent> pendingEvents;

    public CameraService(Camera camera) {
        super("CameraService-" + camera.getId());
        this.camera = camera;
        this.lastProcessedTick = 0;
        this.pendingEvents = new LinkedList<>();
    }

    @Override
    protected void initialize() {
        // Subscribe to TickBroadcast
        subscribeBroadcast(TickBroadcast.class, (TickBroadcast tick) -> {
            int currentTick = tick.getTick();

            // Process pending events from the queue
            while (!pendingEvents.isEmpty()) {
                DetectObjectsEvent event = pendingEvents.peek();
                int detectionTime = event.getTime();

                // Check if the event is ready to be processed
                if (currentTick - camera.getFrequency() >= detectionTime) {
                    sendEvent(event);
                    statfolder.incrementDetectedObjects(event.getDetectedObjects().size());
                    pendingEvents.poll(); // Remove the processed event
                } else {
                    break; // The next event is not ready yet
                }
            }

            // Ensure the service processes new detections only once per tick
            if (currentTick > lastProcessedTick) {
                StampedDetectedObjects detectedObjects = camera.getDetectedObjectsAt(currentTick);

                if (detectedObjects != null) {
                    // Create a DetectObjectsEvent
                    DetectObjectsEvent event = new DetectObjectsEvent(detectedObjects);

                    // Handle frequency logic
                    if (camera.getFrequency() == 0) {
                        // Process immediately if frequency is 0
                        sendEvent(event);
                        statfolder.incrementDetectedObjects(event.getDetectedObjects().size());
                    } else {
                        // Add to the queue with the detection time
                        pendingEvents.offer(event);
                    }
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
