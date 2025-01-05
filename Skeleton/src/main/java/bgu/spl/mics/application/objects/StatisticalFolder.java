package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * StatisticalFolder aggregates and manages statistical data about the system's operation.
 * Tracks runtime, detected/tracked objects, and logs errors during simulation execution.
 */
public class StatisticalFolder {
    private int systemRuntime = 0; // Tracks the total runtime of the system
    private int numDetectedObjects = 0; // Counts the number of detected objects
    private int numTrackedObjects = 0; // Counts the number of tracked objects

    private final List<String> errorLogs = new ArrayList<>(); // Stores error logs
    private StampedDetectedObjects lDetectedObjects = null; // Last detected objects
    private final List<List<TrackedObject>> lTrackedObjects = new ArrayList<>(); // List of last tracked objects
    private final ReentrantLock lock = new ReentrantLock(); // Lock for thread-safe access

    // Private constructor to enforce Singleton pattern
    private StatisticalFolder() {}

    // Singleton Holder: Ensures lazy initialization
    private static class SingletonHolder {
        private static final StatisticalFolder INSTANCE = new StatisticalFolder();
    }

    /**
     * Retrieves the Singleton instance of StatisticalFolder.
     *
     * @return The Singleton instance.
     */
    public static StatisticalFolder getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Increments the system runtime by one tick.
     */
    public void incrementRuntime() {
        lock.lock();
        try {
            systemRuntime++;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Adds a count to the detected objects total.
     *
     * @param count The number of newly detected objects.
     */
    public void incrementDetectedObjects(int count) {
        lock.lock();
        try {
            numDetectedObjects += count;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Adds a count to the tracked objects total.
     *
     * @param count The number of newly tracked objects.
     */
    public void incrementTrackedObjects(int count) {
        lock.lock();
        try {
            numTrackedObjects += count;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Logs an error message to the error log.
     *
     * @param errorMessage The error message to log.
     */
    public void logError(String errorMessage) {
        lock.lock();
        try {
            errorLogs.add(errorMessage);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Retrieves a copy of all error logs.
     *
     * @return A list of error logs.
     */
    public List<String> getErrorLogs() {
        lock.lock();
        try {
            return new ArrayList<>(errorLogs); // Return a copy to maintain thread safety
        } finally {
            lock.unlock();
        }
    }

    /**
     * Prints a summary of system statistics, including runtime and error logs.
     */
    public void printSummary() {
        lock.lock();
        try {
            System.out.println("Simulation Summary:");
            System.out.println("System Runtime: " + systemRuntime);
            System.out.println("Detected Objects: " + numDetectedObjects);
            System.out.println("Tracked Objects: " + numTrackedObjects);
            System.out.println("Landmarks Identified: " + FusionSlam.getInstance().getAllLandmarks().length);
            if (!errorLogs.isEmpty()) {
                System.out.println("Error Logs:");
                for (String log : errorLogs) {
                    System.out.println(" - " + log);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Adds a list of last tracked objects.
     *
     * @param trackedObjects List of tracked objects to add.
     */
    public void addLastTrackedObject(List<TrackedObject> trackedObjects) {
        lock.lock();
        try {
            lTrackedObjects.add(trackedObjects);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Retrieves a copy of the last tracked objects.
     *
     * @return A list of last tracked objects.
     */
    public List<List<TrackedObject>> getLastTrackedObjects() {
        lock.lock();
        try {
            return new ArrayList<>(lTrackedObjects); // Return a copy to maintain thread safety
        } finally {
            lock.unlock();
        }
    }

    /**
     * Retrieves the last detected objects.
     *
     * @return The last detected objects.
     */
    public StampedDetectedObjects getLastDetectedObjects() {
        lock.lock();
        try {
            return lDetectedObjects;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Sets the last detected objects.
     *
     * @param object The last detected objects to set.
     */
    public void setLastDetectedObjects(StampedDetectedObjects object) {
        lock.lock();
        try {
            this.lDetectedObjects = object;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Retrieves the total system runtime.
     *
     * @return The total runtime.
     */
    public int getSystemRuntime() {
        lock.lock();
        try {
            return systemRuntime;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Retrieves the total number of detected objects.
     *
     * @return The total detected objects count.
     */
    public int getNumDetectedObjects() {
        lock.lock();
        try {
            return numDetectedObjects;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Retrieves the total number of tracked objects.
     *
     * @return The total tracked objects count.
     */
    public int getNumTrackedObjects() {
        lock.lock();
        try {
            return numTrackedObjects;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Retrieves the total number of landmarks identified.
     *
     * @return The total landmarks count.
     */
    public int getNumLandmarks() {
        lock.lock();
        try {
            return FusionSlam.getInstance().getAllLandmarks().length;
        } finally {
            lock.unlock();
        }
    }
}
