package bgu.spl.mics.application.objects;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.FileWriter;
import java.io.IOException;

public class output {
    public static void generateOutputFile(String outputFilePath) {
        // Get instances of StatisticalFolder and FusionSlam
        StatisticalFolder stats = StatisticalFolder.getInstance();
        FusionSlam slam = FusionSlam.getInstance();

        // Create a JSON object for the output
        JSONObject output = new JSONObject();

        // Add statistics
        JSONObject statistics = new JSONObject();
        statistics.put("systemRuntime", stats.getSystemRuntime());
        statistics.put("numDetectedObjects", stats.getNumDetectedObjects());
        statistics.put("numTrackedObjects", stats.getNumTrackedObjects());
        statistics.put("numLandmarks", stats.getNumLandmarks());
        output.put("Statistics:", statistics);

        // Add world map landmarks
        JSONArray landmarksArray = new JSONArray();
        for (LandMark landmark : slam.getAllLandmarks()) {
            JSONObject landmarkJson = new JSONObject();
            landmarkJson.put("id", landmark.getId());
            landmarkJson.put("description", landmark.getDescription());
            JSONArray coordinatesArray = new JSONArray();
            for (CloudPoint point : landmark.getCoordinates()) {
                JSONObject pointJson = new JSONObject();
                pointJson.put("x", point.getX());
                pointJson.put("y", point.getY());
                //pointJson.put("z", point.getZ());
                coordinatesArray.put(pointJson);
            }
            landmarkJson.put("coordinates", coordinatesArray);
            landmarksArray.put(landmarkJson);
        }
        output.put("World Map", new JSONObject().put("landMarks", landmarksArray));

        // Write the JSON object to a file
        try (FileWriter file = new FileWriter(outputFilePath)) {
            file.write(output.toString(4)); // Pretty print with an indent of 4 spaces
            System.out.println("Output file successfully created at: " + outputFilePath);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
}
