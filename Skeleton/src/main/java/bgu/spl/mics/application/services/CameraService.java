package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;
import java.util.*;

/**
 * CameraService processes data from the camera and
 * sends DetectObjectsEvent messages to other services.
 */
public class CameraService extends MicroService {
    private static final String ErrorMsg = "ERROR";

    private final Camera camera;
    private int lastProcessedTick; // Tracks the last tick this service processed
    private final StatisticalFolder statisticalFolder = StatisticalFolder.getInstance();

    public CameraService(Camera camera) {
        super("CameraService-" + camera.getId());
        this.camera = camera;
        this.lastProcessedTick = 0; // Start at 0 since no ticks are processed initially
    }

    @Override
    protected void initialize() {
        // Subscribe to TickBroadcast
        subscribeBroadcast(TickBroadcast.class, (TickBroadcast tick) -> {
            int currentTick = tick.getTick();

            // Process data only if we reach the next frequency tick
            if ((currentTick - lastProcessedTick) >= camera.getFrequency()) {
                LinkedList<StampedDetectedObjects> batchedDetections = new LinkedList<>();

                // Collect all detections up to the current tick
                for (int i = lastProcessedTick + 1; i <= currentTick; i++) {
                    StampedDetectedObjects detectedObjects = camera.getDetectedObjectsAt(i);
                    if (detectedObjects != null) {
                        batchedDetections.add(detectedObjects);
                    }
                }

                // Create a single DetectObjectsEvent if there are any detections
                if (!batchedDetections.isEmpty()) {
                    for (StampedDetectedObjects detectedObjects : batchedDetections) {
                        // Check for Error
                        for (DetectedObject d : detectedObjects.getDetectedObjects()) {
                            if (d.getId().equals(ErrorMsg)) {
                                // Log error in statistics
                                statisticalFolder.logError("{CAMERA-" + camera.getId() +
                                        ": Found - " + ErrorMsg +
                                        " in data at time - " + currentTick + "}");

                                // Send crashed broadcast
                                sendBroadcast(new CrashedBroadcast(
                                        "CAMERA-" + camera.getId() + " found error in data"));
                                terminate(); // Shut down on error
                                return;
                            }
                        }

                        // Send event for the batch
                        DetectObjectsEvent event = new DetectObjectsEvent(detectedObjects);
                        sendEvent(event);

                        // Update statistics
                        statisticalFolder.incrementDetectedObjects(detectedObjects.getDetectedObjects().size());
                    }
                }

                // Update last processed tick
                lastProcessedTick = currentTick;
            }
        });

        // Handle termination broadcasts
        subscribeBroadcast(TerminatedBroadcast.class, terminatedBroadcast -> {
            System.out.println(getName() + " received termination signal. Shutting down.");
            terminate();
        });

        // Handle crash broadcasts
        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast -> {
            System.out.println(getName() + " received crash notification: " + crashedBroadcast.getReason());
            terminate();
        });

        System.out.println(getName() + " initialized.");
    }
}
