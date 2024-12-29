package bgu.spl.mics.application;

import java.io.FileReader;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.LiDarDataBase;
// import bgu.spl.mics.application.services.LiDarService;
// import com.google.gson.Gson;
// import com.google.gson.reflect.TypeToken;
// import java.io.FileReader;
// import java.io.IOException;
// import java.lang.reflect.Type;
// import java.util.List;


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
        String config_file = "/workspaces/Assignment-2/Skeleton/example_input_2/configuration_file.json";
        String camera_data = "path";
        String lidar_data = "path";
        String pose_data = "path";

        // Initializing the GPSIMU
        GPSIMU gps = GPSIMU.getInstance();
        gps.Update(pose_data);

        // Initializing the LiDAR DB
        LiDarDataBase lidarDB = LiDarDataBase.getInstance();
        lidarDB.insertWithFile(lidar_data);

        // Initialize the Cameras and LiDAR detectors
        try {
            Gson gson = new Gson();
            FileReader reader = new FileReader(config_file);
            Map<String, Object> config = gson.fromJson(reader, new TypeToken<Map<String, Object>>() {}.getType());
        }
        catch(Exception e) {
            System.out.println("Falied to load the system - error: {" + e + "}");
        }
        
        
        //TODO: initialize services and components
        // TODO: Start the simulation.
    }
}
