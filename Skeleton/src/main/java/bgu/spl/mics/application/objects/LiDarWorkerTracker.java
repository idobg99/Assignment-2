package bgu.spl.mics.application.objects;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

//import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;

import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;

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
            for (int i = time; i > 0; i--) { //IS THIS NEEDED?
                List<StampedCloudPoints> pointsList = lidarDB.getStampedCloudPoints(i);

                for (StampedCloudPoints point : pointsList) {
                    if (point.getId().equals(ErrorMsg)) {
                        statisticalFolder.logError("{LiDAR-" + this.id + ": Found - " + ErrorMsg + 
                                                            " in data at time - " + time + "}");
                        return null;
                    }
                    if (point.getId().equals(id)) {
                        TrackedObject trackedObject = new TrackedObject(point, description);
                        lastTrackedObjects.add(trackedObject);
                        return trackedObject;
                    }
                }                
            }
            statisticalFolder.logError("{LiDAR-" + this.id + ": Error Not Found - " + id + 
                                                            " in data at time - " + time + "}");
            
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

    // public TrackedObjectsEvent processStampedCloudPoints(int currentTime, StampedCloudPoints stampedCloudPoints) {
        
    //     List<TrackedObject> trackedObjects = new ArrayList<>();
    //     for (List<Double> cloudPointData : stampedCloudPoints.getCloudPoints()) {
    //         CloudPoint cloudPoint = new CloudPoint(cloudPointData.get(0).intValue(), cloudPointData.get(1).intValue());
    //         trackedObjects.add(new TrackedObject(stampedCloudPoints.getId(), currentTime, "Tracked from CloudPoints", Arrays.asList(cloudPoint)));
    //     }
    //     this.lastTrackedObjects = trackedObjects;
    //     StatisticalFolder.incrementTrackedObjects(trackedObjects.size());
    //     return new TrackedObjectsEvent(currentTime + frequency, trackedObjects);
    // }


    @Override
    public String toString() {
        return "LiDARTrackerWorker{id=" + id + ", frequency=" + frequency + ", status=" + status + "}";
    }

    public TrackedObjectsEvent DetectTotrackObject (DetectObjectsEvent event){
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
        // } else {
        //     return null;
        }         
    }
    return new TrackedObjectsEvent(event.getTime(), trackedObjects);
}


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
                        TrackedObject trackedObject = new TrackedObject(point, obj.getDescription());
                        lastTrackedObjects.add(trackedObject);
                        trackedObjects.add(trackedObject);
                    }
                }        
    }
    return new TrackedObjectsEvent(event.getTime(), trackedObjects);
}

}
