package bgu.spl.mics.application.objects;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;

/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectObjectsEvents and generates TrackedObjectsEvents using data from the LiDarDataBase.
 * Each worker tracks objects and sends observations to the FusionSlam service.
 */
public class LiDarWorkerTracker {

    private static final String ErrorMsg = "ERROR"; // Constant for error handling

    private final int id; // Unique ID of the LiDAR worker
    private final int frequency; // Frequency of object detection
    private STATUS status; // Status of the worker (e.g., UP or DOWN)
    private List<TrackedObject> lastTrackedObjects; // List of most recently tracked objects
    private final ReentrantLock lock; // Lock for thread safety
    private LiDarDataBase lidarDB = LiDarDataBase.getInstance(); // Singleton instance of LiDAR database
    private StatisticalFolder statisticalFolder = StatisticalFolder.getInstance(); // Singleton for logging

    /**
     * Constructor for LiDarWorkerTracker.
     *
     * @param id        Unique identifier for the worker.
     * @param frequency Frequency of object detection.
     */
    public LiDarWorkerTracker(int id, int frequency) {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP; // Default status is UP
        this.lastTrackedObjects = new ArrayList<>();
        this.lock = new ReentrantLock(); // Initialize the lock
    }

    /**
     * Adds a tracked object to the list of tracked objects.
     *
     * @param object The tracked object to add.
     */
    public void addObject(TrackedObject object) {
        lastTrackedObjects.add(object); // Add the object to the list
    }

    /**
     * Tracks an object by its ID and description, searching through LiDAR data.
     *
     * @param time        The timestamp to search for.
     * @param id          The ID of the object to track.
     * @param description The description of the object.
     * @return The tracked object or null if not found.
     */
    public TrackedObject trackObject(int time, String id, String description) {
        lock.lock(); // Acquire the lock for thread safety
        try {
            for (int i = time; i > 0; i--) { // Search backward in time for matching data
                List<StampedCloudPoints> pointsList = lidarDB.getStampedCloudPoints(i);

                for (StampedCloudPoints point : pointsList) {
                    if (point.getId().equals(ErrorMsg)) {
                        // Log errors if error data is found
                        statisticalFolder.logError("{LiDAR-" + this.id + ": Found - " + ErrorMsg +
                                " in data at time - " + time + "}");
                        return null;
                    }
                    if (point.getId().equals(id)) {
                        // Create and return a tracked object if the ID matches
                        TrackedObject trackedObject = new TrackedObject(point, description);
                        lastTrackedObjects.add(trackedObject);
                        return trackedObject;
                    }
                }
            }
            // Log errors if the object is not found
            statisticalFolder.logError("{LiDAR-" + this.id + ": Error Not Found - " + id +
                    " in data at time - " + time + "}");
            return null;
        } finally {
            lock.unlock(); // Always release the lock
        }
    }

    // Getters and setters for worker properties
    public int getId() {
        return id;
    }

    public int getFrequency() {
        return frequency;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    /**
     * Retrieves the most recently tracked objects.
     *
     * @return An unmodifiable list of last tracked objects.
     */
    public List<TrackedObject> getLastTrackedObjects() {
        return Collections.unmodifiableList(lastTrackedObjects); // Return a safe copy
    }

    @Override
    public String toString() {
        return "LiDARTrackerWorker{id=" + id + ", frequency=" + frequency + ", status=" + status + "}";
    }

    /**
     * Detects and tracks objects based on a DetectObjectsEvent.
     *
     * @param event The DetectObjectsEvent containing detected objects data.
     * @return A TrackedObjectsEvent containing tracked objects.
     */
    public TrackedObjectsEvent DetectTotrackObject(DetectObjectsEvent event) {
        StampedDetectedObjects detectedObjects = event.getStampedDetectedObjects();
        List<TrackedObject> trackedObjects = new ArrayList<>();

        for (DetectedObject obj : detectedObjects.getDetectedObjects()) {
            TrackedObject trackedObject = trackObject(
                    detectedObjects.getTime(),
                    obj.getId(),
                    obj.getDescription()
            );
            if (trackedObject != null) {
                trackedObjects.add(trackedObject);
            }
        }
        return new TrackedObjectsEvent(event.getTime(), trackedObjects);
    }

    /**
     * Alternative method to detect and track objects with null handling.
     *
     * @param event The DetectObjectsEvent containing detected objects data.
     * @return A TrackedObjectsEvent containing tracked objects, or null if input is invalid.
     */
    public TrackedObjectsEvent DetectTotrackObject2(DetectObjectsEvent event) {
        if (event == null || event.getStampedDetectedObjects() == null) {
            // Handle null inputs appropriately
            return null;
        }

        StampedDetectedObjects detectedObjects = event.getStampedDetectedObjects();
        List<TrackedObject> trackedObjects = new ArrayList<>();

        for (DetectedObject obj : detectedObjects.getDetectedObjects()) {
            List<StampedCloudPoints> pointsList = lidarDB.getStampedCloudPoints(detectedObjects.getTime());
            for (StampedCloudPoints point : pointsList) {
                if (point.getId().equals(obj.getId())) {
                    // Track object and add to list
                    TrackedObject trackedObject = new TrackedObject(point, obj.getDescription());
                    lastTrackedObjects.add(trackedObject);
                    trackedObjects.add(trackedObject);
                }
            }
        }
        return new TrackedObjectsEvent(event.getTime(), trackedObjects);
    }
}
