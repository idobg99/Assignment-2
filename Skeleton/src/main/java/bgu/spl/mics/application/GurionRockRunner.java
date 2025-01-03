package bgu.spl.mics.application;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.LiDarDataBase;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.objects.errorOutput;
import bgu.spl.mics.application.objects.output;
import bgu.spl.mics.application.services.CameraService;
import bgu.spl.mics.application.services.FusionSlamService;
import bgu.spl.mics.application.services.LiDarService;
import bgu.spl.mics.application.services.PoseService;
import bgu.spl.mics.application.services.TimeService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    //pathes to insert input files:
    ///usr/bin/env /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java -cp /tmp/cp_46t2ak5l8nahg3aighn8giltk.jar bgu.spl.mics.application.GurionRockRunner /workspaces/Assignment-2/Skeleton/example_input/configuration_file.json
    ///usr/bin/env /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java -cp /tmp/cp_46t2ak5l8nahg3aighn8giltk.jar bgu.spl.mics.application.GurionRockRunner /workspaces/Assignment-2/Skeleton/example_input_2/configuration_file.json
    public static void main(String[] args) {
        String config_file = args[0];  
        File inputfile = new File(config_file);
        String directory = inputfile.getParent();            
        ExecutorService threadPool = Executors.newCachedThreadPool();
 
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(new File(config_file));

            // Initializing the LiDAR DB:   
            String LidarDataPath = rootNode.path("LiDarWorkers").path("lidars_data_path").asText();
            LiDarDataBase lidarDataBase = LiDarDataBase.getInstance();
            lidarDataBase.insertWithFile(directory+ "/lidar_data.json");  //change it to LidarDataPath if the path wil be correct.          
            
            // Parse Cameras:
            JsonNode cameraConfig = rootNode.path("Cameras").path("CamerasConfigurations");
            String cameraDataPath = rootNode.path("Cameras").path("camera_datas_path").asText();
            JsonNode cameraData = mapper.readTree(new File(directory+ "/camera_data.json")); //change it to cameraDataPath if the path wil be correct.                     

            for (JsonNode config : cameraConfig) {
                int id = config.get("id").asInt();
                int frequency = config.get("frequency").asInt();
                String cameraKey = config.get("camera_key").asText();
                //String cameraKey = config.get("camera_datas_path").asText();

                //System.out.println("CAMERAKEY: " + cameraKey);

                JsonNode detectedData = cameraData.path(cameraKey); // List of the stamped objects
                Camera camera = new Camera(id, frequency,detectedData);

                System.out.println("CAMERA - " + camera.getId());
                
                threadPool.submit(new CameraService(camera));
            } 
            
            // Parse LiDar Workers
            JsonNode lidarConfigs = rootNode.path("LiDarWorkers").path("LidarConfigurations");
            for (JsonNode config : lidarConfigs) {
                int id = config.get("id").asInt();
                int frequency = config.get("frequency").asInt();
                LiDarWorkerTracker lidar = new LiDarWorkerTracker(id, frequency);
                
                System.out.println("LIDAR - " + lidar.getId());

                threadPool.submit(new LiDarService(lidar));
            }

            // initializing Poses and fusionSlam::
            GPSIMU gps = GPSIMU.getInstance();
            String PoseDataPath = rootNode.path("poseJsonFile").asText(); 
            gps.Update(directory+ "/pose_data.json");  //change it to PoseDataPath if the path wil be correct.
            threadPool.submit(new PoseService(gps));

            System.out.println("STARTO********************** " + Camera.TOTAL_DETECTED_OBJECTS);

            FusionSlam.getInstance().setLastDetection(Camera.TOTAL_DETECTED_OBJECTS);

            threadPool.submit(new FusionSlamService(FusionSlam.getInstance()));

            //initializing given times:
            int TickTime = rootNode.path("TickTime").asInt();
            int Duration = rootNode.path("Duration").asInt();
            threadPool.submit(new TimeService(TickTime,Duration));   
             
            threadPool.shutdown();

            try {
                if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) { // Wait for up to 60 seconds
                    System.err.println("Some threads did not terminate in the expected time.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Preserve interrupt status
                System.err.println("Thread pool interrupted while waiting for termination.");
            }

            System.out.println("SHUTDOWN");
            // if (threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
            //     System.out.println("All tasks have finished.");
            // } else {
            //     System.out.println("Timeout: Some tasks may not have finished.");
            // }  
        } catch (Exception e) {
            System.err.println("Error reading JSON file: " + e.getMessage());
        }
        

        //creating outputFile:
        File outputFile = new File(directory, "output_file_new.json");
        if (!StatisticalFolder.getInstance().getErrorLogs().isEmpty()){
            System.out.println("ERR");
            errorOutput.generateOutputFile(outputFile.getAbsolutePath());
        }
        else{
            System.out.println("OUT");
            output.generateOutputFile(outputFile.getAbsolutePath());
        }        
    }
}