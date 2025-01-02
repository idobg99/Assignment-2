package bgu.spl.mics.application.objects;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class errorOutput {
    public static void generateOutputFile(String outputFilePath) {
        
        StatisticalFolder stats = StatisticalFolder.getInstance();
        FusionSlam slam = FusionSlam.getInstance();

        // Create a JSON object for the output
        JSONObject output = new JSONObject();

        //add error details
        JSONObject errorsDetails = new JSONObject();
        errorsDetails.put("Error source:", stats.getErrorLogs().get(0));
        errorsDetails.put("Canera last frame:", stats.getLastDetectedObjects());
        errorsDetails.put("Lidar Last Objects:","");
        for (List<TrackedObject> list : stats.getLastTrackedObjects()){
            for (TrackedObject object: list){
                errorsDetails.put("",object.getId());
            }
        output.put("errorsDetails:", errorsDetails);

        // Add Poses:
        JSONArray PosesArray = new JSONArray();
        for (Pose pose : slam.getPreviousPoses()) {
            JSONObject landmarkJson = new JSONObject();
            landmarkJson.put("id", pose.toString());           
            PosesArray.put(landmarkJson);
        }
        output.put("Poses:", new JSONObject().put("", PosesArray));

        // Add statistics
        JSONObject statistics = new JSONObject();
        statistics.put("systemRuntime", stats.getSystemRuntime());
        statistics.put("numDetectedObjects", stats.getNumDetectedObjects());
        statistics.put("numTrackedObjects", stats.getNumTrackedObjects());
        statistics.put("numLandmarks", stats.getNumLandmarks());
        output.put("Statistics:", statistics);

        // Write the JSON object to a file
        try (FileWriter file = new FileWriter(outputFilePath)) {
            file.write(output.toString(4)); // Pretty print with an indent of 4 spaces
            System.out.println("Output file successfully created at: " + outputFilePath);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
}
    
}
