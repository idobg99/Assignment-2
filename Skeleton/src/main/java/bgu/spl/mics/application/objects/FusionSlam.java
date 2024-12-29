package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;
//import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
//@SuppressWarnings("unchecked")
public class FusionSlam {
    // Singleton instance holder
    private static class FusionSlamHolder {
        private static final FusionSlam INSTANCE = new FusionSlam();
    }

    private final List<LandMark> landmarks;
    private final List<Pose> previousPoses;
    private final ReadWriteLock lock;

    private FusionSlam() { // private constructor for the singleton class
        landmarks = new ArrayList<>();
        lock = new ReentrantReadWriteLock();
        previousPoses = new ArrayList<>();
    }

    public static FusionSlam getInstance() {
        return FusionSlamHolder.INSTANCE;
    }

    // Insert a landmark into the landmarks array with collision handling
    public void insertLandmark(LandMark landmark) {
        lock.writeLock().lock();
        try {
            for (int i = 0; i < landmarks.size(); i++) {
                if (landmarks.get(i).getId().equals(landmark.getId())) {
                    landmarks.set(i, landmark);
                    return;
                }
            }
            landmarks.add(landmark);
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    // Get a specific landmark by index
    public LandMark getLandmark(int index) {
        lock.readLock().lock();
        try {
            return landmarks.get(index);
        } finally {
            lock.readLock().unlock();
        }
    }

    // Get all landmarks 
    public LandMark[] getAllLandmarks() {
        List<LandMark> snapshot;
        lock.readLock().lock();
        try {
            snapshot = new ArrayList<>(landmarks); //copy
        } finally {
            lock.readLock().unlock();
        }
        return snapshot.toArray(new LandMark[0]); // Convert to array
    }

    // Add a new pose to the previousPoses list
    public void addPose(Pose pose) {
        lock.writeLock().lock();
        try {
            previousPoses.add(pose);
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    // Get the list of all previous poses
    public List<Pose> getPreviousPoses() {
        lock.readLock().lock();
        try{
            return new ArrayList<>(previousPoses);
        }
        finally {
            lock.readLock().unlock();
        }
    }

    public Pose getCurrentPose() {
        lock.readLock().lock();
        try {
            return previousPoses.get(previousPoses.size() - 1);
        }
        finally {
            lock.readLock().unlock();
        }
    }

    // Clear all poses
    public void clearPoses() {
        lock.writeLock().lock();
        try {
            previousPoses.clear();
        }
        finally {
            lock.writeLock().unlock();
        }
    }
}
