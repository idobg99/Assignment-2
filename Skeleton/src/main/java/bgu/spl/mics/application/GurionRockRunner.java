package bgu.spl.mics.application;


import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.LiDarDataBase;
import bgu.spl.mics.MicroService;

// import com.google.gson.Gson;
// import com.google.gson.reflect.TypeToken;
// import java.io.FileReader;
// import java.io.IOException;
// import java.lang.reflect.Type;
// import java.util.List;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.services.CameraService;
import bgu.spl.mics.application.services.FusionSlamService;
import bgu.spl.mics.application.services.LiDarService;
import bgu.spl.mics.application.services.PoseService;
import bgu.spl.mics.application.services.TimeService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;




/**
 * The main entry point for the GurionRock Pro Max Ultra Over 9000 simulation.
 * <p>
 * This class initializes the system and starts the simulation by setting up
 * services, objects, and configurations.
 * </p>
 */
public class GurionRockRunner {

    /**
     * The main method of the simulation.
     * This method sets up the necessary components, parses configuration files,
     * initializes services, and starts the simulation.
     *
     * @param args Command-line arguments. The first argument is expected to be the path to the configuration file.
     */
    public static void main(String[] args) {
        System.out.println("Hello World!");

        // Parse configuration file
        //String config_file = "/workspaces/Assignment-2/Skeleton/example_input_2/configuration_file.json";
        String config_file = args[0];
        
        ExecutorService threadPool = Executors.newFixedThreadPool(20);

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(new File(config_file));

            // initializing Poses and fusionSlam::
            GPSIMU gps = GPSIMU.getInstance();
            String PoseDataPath = rootNode.path("poseJsonFile").asText(); 
            gps.Update(PoseDataPath);
            threadPool.submit(new PoseService(gps));
            threadPool.submit(new FusionSlamService(FusionSlam.getInstance()));

            // Initializing the LiDAR DB:   
            String LidarDataPath = rootNode.path("LiDarWorkers").path("lidars_data_path").asText();
            LiDarDataBase lidarDataBase = LiDarDataBase.getInstance();
            lidarDataBase.insertWithFile(LidarDataPath);          

            // Parse Cameras:
            JsonNode cameraConfig = rootNode.path("Cameras").path("CamerasConfigurations");
            String cameraDataPath = rootNode.path("Cameras").path("camera_datas_path").asText();
            JsonNode cameraData = mapper.readTree(new File(cameraDataPath));            

            for (JsonNode config : cameraConfig) {
                int id = config.get("id").asInt();
                int frequency = config.get("frequency").asInt();
                String cameraKey = config.get("camera_key").asText();
                JsonNode detectedData = cameraData.path(cameraKey);
                Camera camera = new Camera(id, frequency,detectedData);
                threadPool.submit(new CameraService(camera));
            } 
            
            // Parse LiDar Workers
            JsonNode lidarConfigs = rootNode.path("LiDarWorkers").path("LidarConfigurations");
            for (JsonNode config : lidarConfigs) {
                int id = config.get("id").asInt();
                int frequency = config.get("frequency").asInt();
                LiDarWorkerTracker lidar = new LiDarWorkerTracker(id, frequency);
                threadPool.submit(new LiDarService(lidar));
            }

            //initializing given times:
            int TickTime = rootNode.path("TickTime").asInt();
            int Duration = rootNode.path("Duration").asInt();
            threadPool.submit(new TimeService(TickTime,Duration));   
            
            
            threadPool.shutdown();
            if (threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                System.out.println("All tasks have finished.");
            } else {
                System.out.println("Timeout: Some tasks may not have finished.");
            }

            


        } catch (Exception e) {
            System.err.println("Error reading JSON file: " + e.getMessage());
        }


        
        // // Initializing the GPSIMU
        // GPSIMU gps = GPSIMU.getInstance();
        // gps.Update(pose_data);

        // Initializing the LiDAR DB
        // LiDarDataBase lidarDB = LiDarDataBase.getInstance();
        // lidarDB.insertWithFile(lidar_data);

        // // Initialize the Cameras and LiDAR detectors
        // try {
        //     Gson gson = new Gson();
        //     FileReader reader = new FileReader(config_file);
        //     Map<String, Object> config = gson.fromJson(reader, new TypeToken<Map<String, Object>>() {}.getType());
        // }
        // catch(Exception e) {
        //     System.out.println("Falied to load the system - error: {" + e + "}");
        // }


        

        
        
        //TODO: initialize services and components
        // TODO: Start the simulation.
    }
}