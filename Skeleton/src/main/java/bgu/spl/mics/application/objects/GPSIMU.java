package bgu.spl.mics.application.objects;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */
public class GPSIMU {
    private volatile int currentTick;
    private volatile STATUS status;
    private volatile CopyOnWriteArrayList<Pose> poseList;

    public GPSIMU(String Pose_data_filePath) {
        this.currentTick = 0;
        this.status = STATUS.UP;
        //this.poseList = new CopyOnWriteArrayList<>();

        try {   //parsing the jason file
            Gson gson = new Gson();
            FileReader reader = new FileReader(Pose_data_filePath);
            TypeToken<List<Pose>> typeToken = new TypeToken<List<Pose>>() {};
            this.poseList = gson.fromJson(reader, typeToken.getType());
        }
        catch (Exception e) {};  
}
        

    public synchronized void updatePose(Pose pose) {
        poseList.add(pose);
        currentTick = pose.getTime();
    }

    public synchronized Pose getCurrentPose() {
        return poseList.isEmpty() ? null : poseList.get(poseList.size() - 1);
    }

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
        return "GPSIMU{currentTick=" + currentTick + ", status=" + status + ", poseList=" + poseList + "}";
    }
}