package bgu.spl.mics.application.objects;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam {
    // Singleton instance holder
    private static class FusionSlamHolder {
        private static FusionSlam INSTANCE = new FusionSlam();
    }

    private volatile LandMark[] landmarks;
    private volatile CopyOnWriteArrayList<Pose> previousPoses;
    private final ReadWriteLock lock;

    private FusionSlam() { // private constructor for the singleton class
        landmarks = new LandMark[1000];
        lock = new ReentrantReadWriteLock();
        previousPoses = new CopyOnWriteArrayList<>();
    }

    public static FusionSlam getInstance() {
        return FusionSlamHolder.INSTANCE;
    }

    // Insert a landmark into the landmarks array at a specific index
    public void insertLandmark(int index, LandMark landmark) {
        lock.writeLock().lock();
        try {
            if (index >= 0 && index < landmarks.length) {
                landmarks[index] = landmark;
            } else {
                throw new IndexOutOfBoundsException("Invalid landmark index: " + index);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    // Get a specific landmark by index
    public LandMark getLandmark(int index) {
        lock.readLock().lock();
        try {
            if (index >= 0 && index < landmarks.length) {
                return landmarks[index];
            } else {
                throw new IndexOutOfBoundsException("Invalid landmark index: " + index);
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    // Get all landmarks (returns a copy of the array)
    public LandMark[] getAllLandmarks() {
        lock.readLock().lock();
        try {
            return landmarks.clone();
        } finally {
            lock.readLock().unlock();
        }
    }

    // Add a new pose to the previousPoses list
    public void addPose(Pose pose) {
        previousPoses.add(pose);
    }

    // Get the list of all previous poses (returns an immutable copy)
    public CopyOnWriteArrayList<Pose> getPreviousPoses() {
        return new CopyOnWriteArrayList<>(previousPoses);
    }

    // Clear all poses
    public void clearPoses() {
        previousPoses.clear();
    }
}
