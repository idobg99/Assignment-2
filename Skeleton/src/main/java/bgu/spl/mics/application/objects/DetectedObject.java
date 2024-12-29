package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * Represents an object detected by a camera.
 */
public class DetectedObject {
    private final String id;
    private final String description;
    //private final List<CloudPoint> pointCloud;

    public DetectedObject(String id, String description) {
        this.id = id;
        this.description = description;
        //this.pointCloud = pointCloud;

    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    //public List<CloudPoint> getPointCloud() {
    //    return pointCloud;
    //}


    @Override
    public String toString() {
        return "DetectedObject{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                //", pointCloud=" + pointCloud +
                '}';
    }
}