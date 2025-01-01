package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;

import java.util.LinkedList;
import java.util.Queue;

/**
 * CameraService processes data from the camera and
 * sends DetectObjectsEvent messages to other services.
 */
public class CameraService extends MicroService {
    private static final String ErrorMsg = "ERROR";

    private final Camera camera;
    private int lastProcessedTick; // Tracks the last tick this service processed
    private StatisticalFolder statfolder = StatisticalFolder.getInstance();
    private StatisticalFolder statisticalFolder = StatisticalFolder.getInstance();

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

            System.out.println("TICKKKKKK _CAMERA - " + currentTick);

            // Process pending events from the queue
            while (!pendingEvents.isEmpty()) {

                System.out.println("TEST PENDING QUEUE");


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


                    System.out.println("CAMERA DETECTED!!!!!!!!!!!!!!!! - " + detectedObjects.toString());

                    // Check for Error
                    for (DetectedObject d : detectedObjects.getDetectedObjects()) {
                        if (d.getId().equals(ErrorMsg)) {
                            
                            // Log in statistics 
                            statisticalFolder.logError("{" + camera.getId() + ": Found - " + ErrorMsg + 
                                                            " in data at time - " + currentTick + "}");

                            // Send crashed broadcast
                            sendBroadcast(new CrashedBroadcast(camera.getId() + "found error in data"));
                        }
                    }


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
        subscribeBroadcast(TerminatedBroadcast.class, terminatedBroadcast -> {
            System.out.println(getName() + " received termination signal. Shutting down.");
            terminate();
        });

        // Handle CrashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast -> {
            System.err.println(getName() + " received crash notification: " + crashedBroadcast.getReason());
            statisticalFolder.setlDetectedObjects(camera.GetLastDetectedObjects());
            terminate();
            // Perform any cleanup or map adjustment due to crash
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
