package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;
//import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.Arrays;


import java.util.Map;
import java.util.HashMap;

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
    private Map<Integer, Pose> poses;
    private final ReadWriteLock lock;
    private int lastDetectionTime = -1;

    private FusionSlam() { // private constructor for the singleton class
        landmarks = new ArrayList<>();
        lock = new ReentrantReadWriteLock();
        previousPoses = new ArrayList<>();
        poses = new HashMap<>();
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
            poses.putIfAbsent(pose.getTime(), pose);
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

    public Pose getPoseAt(int time) {
        lock.readLock().lock();
        try {
            return poses.get(time);
        } 
        catch (Exception e) {
            System.out.println(e);
            return null;
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

    public void setLastDetectionTime(int time) {
        this.lastDetectionTime = time;
    }

    public int getLastDetectionTime() {
        return this.lastDetectionTime;
    }

    public LandMark calculteLandMark(TrackedObject trackedObject, Pose currentPose) {    
        if (currentPose == null) {
                return null; }       
        List<CloudPoint> coordinates = new ArrayList<>();
        for (CloudPoint tobj : trackedObject.getCoordinates()) {
            double xGlobal = currentPose.getX() + (tobj.getX() * Math.cos(Math.toRadians(currentPose.getYaw())) -
                                                   tobj.getY() * Math.sin(Math.toRadians(currentPose.getYaw())));
            double yGlobal = currentPose.getY() + (tobj.getX() * Math.sin(Math.toRadians(currentPose.getYaw())) +
                                                   tobj.getY() * Math.cos(Math.toRadians(currentPose.getYaw())));
            coordinates.add(new CloudPoint(xGlobal, yGlobal));
        }

        LandMark newLandmark = new LandMark(trackedObject.getId(),
                                                    trackedObject.getDescription(),
                                                    coordinates);
         return newLandmark;
    }
}