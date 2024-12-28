package bgu.spl.mics.application.objects;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Holds statistical information about the system's operation.
 * This class aggregates metrics such as the runtime of the system,
 * the number of objects detected and tracked, and the number of landmarks identified.
 */
public class StatisticalFolder {
    private int systemRuntime = 0;
    private int numDetectedObjects = 0;
    private int numTrackedObjects = 0;
    private int numLandmarks = 0;
    private final ReentrantLock lock = new ReentrantLock();

    // Private constructor to prevent instantiation
    private StatisticalFolder() {}

    // Singleton holder
    private static class SingletonHolder {
        private static final StatisticalFolder INSTANCE = new StatisticalFolder();
    }

    // Method to get the singleton instance
    public static StatisticalFolder getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void incrementRuntime() {
        lock.lock();
        try {
            systemRuntime++;
        } finally {
            lock.unlock();
        }
    }

    public void incrementDetectedObjects(int count) {
        lock.lock();
        try {
            numDetectedObjects += count;
        } finally {
            lock.unlock();
        }
    }

    public void incrementTrackedObjects(int count) {
        lock.lock();
        try {
            numTrackedObjects += count;
        } finally {
            lock.unlock();
        }
    }

    public void incrementLandmarks() {
        lock.lock();
        try {
            numLandmarks++;
        } finally {
            lock.unlock();
        }
    }

    public void printSummary() {
        lock.lock();
        try {
            System.out.println("Simulation Summary:");
            System.out.println("System Runtime: " + systemRuntime);
            System.out.println("Detected Objects: " + numDetectedObjects);
            System.out.println("Tracked Objects: " + numTrackedObjects);
            System.out.println("Landmarks Identified: " + numLandmarks);
        } finally {
            lock.unlock();
        }
    }
}
