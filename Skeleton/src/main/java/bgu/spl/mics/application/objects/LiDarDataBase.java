package bgu.spl.mics.application.objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {
    private final Map<Integer, List<StampedCloudPoints>> cloudPointsMap;
    private final ReentrantReadWriteLock lock; // Lock for thread-safety

    private LiDarDataBase() {
        this.cloudPointsMap = new HashMap<>();
        this.lock = new ReentrantReadWriteLock(); // Initialize the lock
    }

    // public void insertWithFile(String filePath) {
    //     lock.writeLock().lock(); // Acquire write lock
    //     try {
    //         // Parse JSON file into a list of StampedCloudPoints
    //         Gson gson = new Gson();
    //         FileReader reader = new FileReader(filePath);
    //         List<StampedCloudPoints> stampedCloudPointsList = gson.fromJson(
    //                 reader,
    //                 new TypeToken<List<StampedCloudPoints>>() {}.getType()
    //         );

    //         // Group StampedCloudPoints by time
    //         for (StampedCloudPoints stampedCloudPoints : stampedCloudPointsList) {
    //             cloudPointsMap
    //                     .computeIfAbsent(stampedCloudPoints.getTime(), k -> new ArrayList<>())
    //                     .add(stampedCloudPoints);
    //         }
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     } finally {
    //         lock.writeLock().unlock(); // Release write lock
    //     }
    // }

    public void insertWithFile(String filePath) throws IOException {
        File file = new File(filePath);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(file);

        lock.writeLock().lock();
        for (JsonNode node : rootNode) {
            int time = node.get("time").asInt();
            String id = node.get("id").asText();

            List<CloudPoint> cloudPoints = new ArrayList<>();
            for (JsonNode point : node.get("cloudPoints")) {
                double x = point.get(0).asDouble();
                double y = point.get(1).asDouble();
                cloudPoints.add(new CloudPoint(x, y /*, z*/));
            }

            StampedCloudPoints stampedCloudPoints = new StampedCloudPoints(time, id, cloudPoints);
            this.cloudPointsMap.computeIfAbsent(time, k -> new ArrayList<>()).add(stampedCloudPoints);
        }
        lock.writeLock().unlock();
    }

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

    // Singleton fields and access
    private static class SingletonHolder {
        private static volatile LiDarDataBase INSTANCE = new LiDarDataBase();
    }

    public static LiDarDataBase getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Retrieves all stamped cloud points at a given time.
     *
     * @param time The timestamp to query.
     * @return A list of stamped cloud points at the given time.
     */
    public List<StampedCloudPoints> getStampedCloudPoints(int time) {
        try {
            //System.out.println("WE ARE HERE **********************");
            lock.readLock().lock(); // Acquire read lock
            
            // Return a copy to avoid exposing internal state
            return new ArrayList<>(cloudPointsMap.getOrDefault(time, Collections.emptyList()));
        }
         catch (Exception e)  {
            System.out.println(e);
            return null;
        }
         finally {
            //System.out.println("WE ARE HERE2 **********************");
            lock.readLock().unlock(); // Release read lock
        }
    }

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
