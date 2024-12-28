package bgu.spl.mics.application.objects;

import java.io.FileReader;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */
public class GPSIMU {
    private volatile int currentTick; // Current tick of the robot
    private volatile STATUS status; // Status of the GPS/IMU system
    private final CopyOnWriteArrayList<Pose> poseList; // List of all poses sorted by time

    public GPSIMU(String poseDataFilePath) {
        this.currentTick = 0;
        this.status = STATUS.UP;
        this.poseList = new CopyOnWriteArrayList<>();

        try {
            // Parse the JSON file to load pose data
            Gson gson = new Gson();
            FileReader reader = new FileReader(poseDataFilePath);
            TypeToken<List<Pose>> typeToken = new TypeToken<List<Pose>>() {};
            List<Pose> loadedPoses = gson.fromJson(reader, typeToken.getType());
            if (loadedPoses != null) {
                this.poseList.addAll(loadedPoses);
            }
        } catch (Exception e) {
            System.err.println("Failed to load pose data from file: " + e.getMessage());
        }
    }

    /**
     * Retrieves the pose corresponding to the given tick.
     *
     * @param tick The tick for which the pose is requested.
     * @return The pose at the given tick, or null if no such pose exists.
     */
    public Pose getPoseAtTick(int tick) {  //COULD BE HASHMAP////////////////////////////
        for (Pose pose : poseList) {
            if (pose.getTime() == tick) {
                return pose;
            }
        }
        return null; // Return null if no pose matches the given tick
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
                ", poseList=" + poseList +
                '}';
    }
}
