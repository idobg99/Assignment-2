package bgu.spl.mics.application.objects;

public class Config {
    CamerasConfig Cameras;
    LidarsConfig Lidars;
    String poseJsonFile;
    int TickTime;
    int Duration;

    public Config() {
    }

    public CamerasConfig getCamerasConfig() {
        return Cameras;
    }

    public LidarsConfig getLidarsConfig() {
        return Lidars;
    }

    public String getPoseJson(){
        return poseJsonFile;
    }

    public int getTickTime() {
        return TickTime;
    }

    public int getDuration() {
        return Duration;
    }
}
