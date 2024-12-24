package bgu.spl.mics.application.objects;

import java.util.*;
import java.util.concurrent.locks.*;

/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */
public class Camera {
    private int id;
    private int frequency;
    private STATUS status;
    private List<StampedDetectedObjects> detectedObjectsList;
    private final Lock lock; 

    public Camera(int id, int frequency) {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP;
        this.detectedObjectsList = new ArrayList<>();
        this.lock = new ReentrantLock();
    }

    
    public void addDetectedObjects(int time, List<DetectedObject> detectedObjects) {
        lock.lock();
        try {
            detectedObjectsList.add(new StampedDetectedObjects(time, detectedObjects));
            StatisticalFolder.incrementDetectedObjects(detectedObjects.size());
        } finally {
            lock.unlock();
        }
    }

    public List<StampedDetectedObjects> getDetectedObjectsSince(int lastTick) {
        lock.lock();
        try {
            List<StampedDetectedObjects> recentObjects = new ArrayList<>();
            for (StampedDetectedObjects entry : detectedObjectsList) {
                if (entry.getTime() >= lastTick) {
                    recentObjects.add(entry);
                }
            }
            return recentObjects;
        } finally {
            lock.unlock();
        }
    }

    public int getId() {
        return id;
    }

    public int getFrequency() {
        return frequency;
    }

    public STATUS getStatus() {
        lock.lock();
        try {
            return status;
        } finally {
            lock.unlock();
        }
    }

    public void setStatus(STATUS status) {
        lock.lock();
        try {
            this.status = status;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String toString() {
        lock.lock();
        try {
            return "Camera{id=" + id + ", frequency=" + frequency + ", status=" + status + ", detectedObjectsList=" + detectedObjectsList + "}";
        } finally {
            lock.unlock();
        }
    }
}


