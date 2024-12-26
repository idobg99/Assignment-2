package bgu.spl.mics.application.objects;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {
    /**
     * Returns the singleton instance of LiDarDataBase.
     *
     * @param filePath The path to the LiDAR data file.
     * @return The singleton instance of LiDarDataBase.
     */
    public static LiDarDataBase getInstance(String filePath) {
        // TODO: Implement this
        return SingletonHolder.INSTANCE;

    }
//fields and static class for the singelton:
    private static class SingletonHolder {
        private static LiDarDataBase INSTANCE = new LiDarDataBase();
    }
    private List<StampedCloudPoints> CloudPoints;
    
    private LiDarDataBase() {
        this.CloudPoints = new CopyOnWriteArrayList<>();
    }

    public void addStampedCloudPoints(StampedCloudPoints newStamp){
        CloudPoints.add(newStamp);
    }    
}
