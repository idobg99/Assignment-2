package bgu.spl.mics.application.objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides thread-safe access to cloud point data and related information for tracked objects.
 */
public class LiDarDataBase {
    private final Map<Integer, List<StampedCloudPoints>> cloudPointsMap; // Maps time to lists of stamped cloud points
    private final ReentrantReadWriteLock lock; // Read-write lock for thread-safe access

    // Private constructor for Singleton
    private LiDarDataBase() {
        this.cloudPointsMap = new HashMap<>();
        this.lock = new ReentrantReadWriteLock(); // Initialize the lock
    }

    /**
     * Inserts data from a JSON file into the database.
     *
     * @param filePath The path to the JSON file containing LiDAR data.
     * @throws IOException If there is an error reading the file.
     */
    public void insertWithFile(String filePath) throws IOException {
        File file = new File(filePath);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(file);

        lock.writeLock().lock(); // Acquire write lock
        try {
            for (JsonNode node : rootNode) {
                int time = node.get("time").asInt(); // Extract timestamp
                String id = node.get("id").asText(); // Extract ID

                // Parse cloud points
                List<CloudPoint> cloudPoints = new ArrayList<>();
                for (JsonNode point : node.get("cloudPoints")) {
                    double x = point.get(0).asDouble();
                    double y = point.get(1).asDouble();
                    cloudPoints.add(new CloudPoint(x, y /*, z*/)); // Add parsed point
                }

                // Create stamped cloud points and add them to the map
                StampedCloudPoints stampedCloudPoints = new StampedCloudPoints(time, id, cloudPoints);
                this.cloudPointsMap.computeIfAbsent(time, k -> new ArrayList<>()).add(stampedCloudPoints);
            }
        } finally {
            lock.writeLock().unlock(); // Release write lock
        }
    }

    /**
     * Inserts a single stamped cloud point into the database.
     *
     * @param stampedCloudPoints The stamped cloud point to insert.
     */
    public void insertSingular(StampedCloudPoints stampedCloudPoints) {
        lock.writeLock().lock(); // Acquire write lock
        try {
            cloudPointsMap
                .computeIfAbsent(stampedCloudPoints.getTime(), k -> new ArrayList<>())
                .add(stampedCloudPoints);
        } finally {
            lock.writeLock().unlock(); // Release write lock
        }
    }

    // Static inner class for lazy-loaded Singleton instance
    private static class SingletonHolder {
        private static volatile LiDarDataBase INSTANCE = new LiDarDataBase();
    }

    /**
     * Provides the Singleton instance of the LiDarDataBase.
     *
     * @return The Singleton instance.
     */
    public static LiDarDataBase getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Retrieves all stamped cloud points at a specific timestamp.
     *
     * @param time The timestamp to query.
     * @return A list of stamped cloud points at the given time.
     */
    public List<StampedCloudPoints> getStampedCloudPoints(int time) {
        lock.readLock().lock(); // Acquire read lock
        try {
            // Return a defensive copy to avoid modifying internal state
            return new ArrayList<>(cloudPointsMap.getOrDefault(time, Collections.emptyList()));
        } catch (Exception e) {
            System.out.println(e); // Log exceptions
            return null;
        } finally {
            lock.readLock().unlock(); // Release read lock
        }
    }

    /**
     * Provides a string representation of the LiDarDataBase.
     *
     * @return A string describing the database.
     */
    @Override
    public String toString() {
        lock.readLock().lock(); // Acquire read lock
        try {
            return "LiDarDataBase{" +
                    "cloudPointsMap=" + cloudPointsMap +
                    '}';
        } finally {
            lock.readLock().unlock(); // Release read lock
        }
    }
}
