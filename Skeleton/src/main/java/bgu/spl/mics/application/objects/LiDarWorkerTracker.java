package bgu.spl.mics.application.objects;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;

import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;

//import bgu.spl.mics.application.messages.CrashedBroadcast;


/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectObjectsEvents and generates TrackedObjectsEvents by using data from the LiDarDataBase.
 * Each worker tracks objects and sends observations to the FusionSlam service.
 */
public class LiDarWorkerTracker {

    private static final String ErrorMsg = "ERROR";

    private final int id;
    private final int frequency;
    private STATUS status;
    private List<TrackedObject> lastTrackedObjects;
    private final ReentrantLock lock;
    private LiDarDataBase lidarDB = LiDarDataBase.getInstance();
    private StatisticalFolder statisticalFolder = StatisticalFolder.getInstance();

    public LiDarWorkerTracker(int id, int frequency) {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP;
        this.lastTrackedObjects = new ArrayList<>();
        this.lock = new ReentrantLock();
        //LiDarDataBase lidarDB = LiDarDataBase.getInstance();
    }

    public void addObject(TrackedObject object){
        //synchronized(lastTrackedObjects){
        lastTrackedObjects.add(object);
        //} 
    }

    public TrackedObject trackObject(int time, String id, String description) {
        lock.lock();
        try {
            List<StampedCloudPoints> pointsList = lidarDB.getStampedCloudPoints(time);

            for (StampedCloudPoints point : pointsList) {
                if (point.getId().equals(ErrorMsg)) {
                    statisticalFolder.logError("{" + this.id + ": Found - " + ErrorMsg + 
                                                        " in data at time - " + time + "}");
                    //sendBroadcast(new CrashedBroadcast(this.id + "found error in data"));
                    return null;
                }
                if (point.getId().equals(id)) {
                    TrackedObject trackedObject = new TrackedObject(point, description);
                    lastTrackedObjects.add(trackedObject);
                    return trackedObject;
                }
            }
            return null;
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
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public List<TrackedObject> getLastTrackedObjects() {
        return Collections.unmodifiableList(lastTrackedObjects);
    }

    @Override
    public String toString() {
        return "LiDARTrackerWorker{id=" + id + ", frequency=" + frequency + ", status=" + status + "}";
    }

<<<<<<< Updated upstream
    public TrackedObjectsEvent DetectTotrackObject (DetectObjectsEvent event){
=======
    public TrackedObjectsEvent process (DetectObjectsEvent event){
>>>>>>> Stashed changes
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
        } else {
            return new TrackedObjectsEvent(event.getTime(), null);
        }         
    }
    return new TrackedObjectsEvent(event.getTime(), trackedObjects);
}
}
