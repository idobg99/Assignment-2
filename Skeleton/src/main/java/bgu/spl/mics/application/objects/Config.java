package bgu.spl.mics.application.objects;
//import java.util.Map;

public class Config {
    CamerasConfig cameras;
    LidarsConfig lidars;
    String poseJsonFile;
    int tickTime;
    int duration;

    public Config() {
    }

    public CamerasConfig getCamerasConfig() {
        return cameras;
    }

    public LidarsConfig getLidarsConfig() {
        return lidars;
    }

    public String getPoseJson(){
        return poseJsonFile;
    }

    public int getTickTime() {
        return tickTime;
    }

    public int getDuration() {
        return duration;
    }
}
