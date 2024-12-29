package bgu.spl.mics.application.objects;

import java.io.FileReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */
public class GPSIMU {
    private volatile int currentTick; // Current tick of the robot
    private volatile STATUS status; // Status of the GPS/IMU system
    private final ConcurrentHashMap<Integer, Pose> poseMap; // Map of poses indexed by tick

    private GPSIMU() {
        this.currentTick = 0;
        this.status = STATUS.UP;
        this.poseMap = new ConcurrentHashMap<>();

        
    }

    public Boolean Update(String poseDataFilePath) {
        try {
            // Parse the JSON file to load pose data
            Gson gson = new Gson();
            FileReader reader = new FileReader(poseDataFilePath);
            TypeToken<List<Pose>> typeToken = new TypeToken<List<Pose>>() {};
            List<Pose> loadedPoses = gson.fromJson(reader, typeToken.getType());
            if (loadedPoses != null) {
                for (Pose pose : loadedPoses) {
                    this.poseMap.put(pose.getTime(), pose);
                }
            }
            return true;
        } catch (Exception e) {
            System.err.println("Failed to load pose data from file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves the pose corresponding to the given tick.
     *
     * @param tick The tick for which the pose is requested.
     * @return The pose at the given tick, or null if no such pose exists.
     */
    public Pose getPoseAtTick(int tick) {
        return poseMap.get(tick); // Efficient lookup using the map
    }

    /**
     * Updates the current tick of the GPS/IMU system.
     *
     * @param tick The new current tick.
     */
    public void updateCurrentTick(int tick) {
        this.currentTick = tick;
    }

    /**
     * Gets the current tick of the robot.
     *
     * @return The current tick.
     */
    public int getCurrentTick() {
        return currentTick;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "GPSIMU{" +
                "currentTick=" + currentTick +
                ", status=" + status +
                ", poseMap=" + poseMap +
                '}';
    }

    // Singleton instance holder
    private static class SingletonHolder {
        private static GPSIMU INSTANCE;

        private static void initialize() {
            if (INSTANCE == null) {
                INSTANCE = new GPSIMU();
            }
        }
    }

    public static GPSIMU getInstance() {
        SingletonHolder.initialize();
        return SingletonHolder.INSTANCE;
    }
}
