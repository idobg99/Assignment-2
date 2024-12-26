package bgu.spl.mics.application.objects;

import java.io.FileReader;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {

    private volatile List<StampedCloudPoints> CloudPoints;
    
    private LiDarDataBase(String filePath) {     
        try {   //parsing the jason file
            Gson gson = new Gson();
            FileReader reader = new FileReader(filePath);
            TypeToken<List<StampedCloudPoints>> typeToken = new TypeToken<List<StampedCloudPoints>>() {};
            this.CloudPoints = gson.fromJson(reader, typeToken.getType());
        }
        catch (Exception e) {};  
}

    //fields and static class for the singelton:
    private static class SingletonHolder {
        public static String filePath;
        private static volatile LiDarDataBase INSTANCE = new LiDarDataBase(filePath);
    }
    /**
     * Returns the singleton instance of LiDarDataBase.
     *
     * @param filePath The path to the LiDAR data file.
     * @return The singleton instance of LiDarDataBase.
     */
    public static LiDarDataBase getInstance(String filePath) {
        SingletonHolder.filePath = filePath;
        return SingletonHolder.INSTANCE;
    }

    public List<List<Double>> getStampedCloudPoints(int time){
        return CloudPoints.get(time).getCloudPoints(); 

    } 



  
}
