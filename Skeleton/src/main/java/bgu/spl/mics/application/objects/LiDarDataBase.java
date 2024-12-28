package bgu.spl.mics.application.objects;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.util.*;

/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {
    private final Map<Integer, List<StampedCloudPoints>> cloudPointsMap;

    private LiDarDataBase() {
        this.cloudPointsMap = new HashMap<>();
    }

    public void insertWithFile(String filePath) {
        try {
            // Parse JSON file into a list of StampedCloudPoints
            Gson gson = new Gson();
            FileReader reader = new FileReader(filePath);
            List<StampedCloudPoints> stampedCloudPointsList = gson.fromJson(
                    reader,
                    new TypeToken<List<StampedCloudPoints>>() {}.getType()
            );

            // Group StampedCloudPoints by time
            for (StampedCloudPoints stampedCloudPoints : stampedCloudPointsList) {
                cloudPointsMap
                        .computeIfAbsent(stampedCloudPoints.getTime(), k -> new ArrayList<>())
                        .add(stampedCloudPoints);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertSingular(StampedCloudPoints stampedCloudPoints) {
        cloudPointsMap
        .computeIfAbsent(stampedCloudPoints.getTime(), k -> new ArrayList<>())
        .add(stampedCloudPoints);
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
        return cloudPointsMap.getOrDefault(time, Collections.emptyList());
    }

    @Override
    public String toString() {
        return "LiDarDataBase{" +
                "cloudPointsMap=" + cloudPointsMap +
                '}';
    }
}
