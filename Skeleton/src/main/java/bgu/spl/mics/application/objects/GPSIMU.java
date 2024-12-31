package bgu.spl.mics.application.objects;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(new File(poseDataFilePath));

            // Iterate over each element in the JSON array
            for (JsonNode node : rootNode) {
                int time = node.get("time").asInt();
                double x = node.get("x").asDouble();
                double y = node.get("y").asDouble();
                float yaw = (float) node.get("yaw").asDouble();

                // Create a Pose and add it to the list
                Pose pose = new Pose(time, x, y, yaw);
                this.poseMap.put(pose.getTime(), pose);
            }
            return true;
        } catch (Exception e) {
            System.err.println("Failed to load poses: " + e.getMessage());
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
