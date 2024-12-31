package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Holds statistical information about the system's operation.
 * This class aggregates metrics such as the runtime of the system,
 * the number of objects detected and tracked, and the number of landmarks identified.
 */
public class StatisticalFolder {
    public int systemRuntime = 0;
    public int numDetectedObjects = 0;
    public int numTrackedObjects = 0;
    public int numLandmarks = 0;

    // Added for error logs:
    private final List<String> errorLogs = new ArrayList<>(); 
    public StampedDetectedObjects lDetectedObjects = null;
    private final List<List<TrackedObject>> lTrackedObjects = new CopyOnWriteArrayList<>();

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

    public void logError(String errorMessage) { // Added method for error logging
        lock.lock();
        try {
            errorLogs.add(errorMessage);
        } finally {
            lock.unlock();
        }
    }

    public List<String> getErrorLogs() { // Added method to retrieve error logs
        lock.lock();
        try {
            return new ArrayList<>(errorLogs); // Return a copy to avoid concurrency issues
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
    public void addLastTrackedObject(List<TrackedObject> T){
        lTrackedObjects.add(T);
    }
    public List<List<TrackedObject>> GetLastTrackedObject(){
        return lTrackedObjects;
    }
    public List<String> getErrorLogs(){
        return errorLogs;
    }

}
