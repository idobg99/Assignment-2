package bgu.spl.mics.application.objects;

public class CamerasConfiguration {
    int id;
    int frequency;
    String camera_key;

    public CamerasConfiguration() {}

    public int getFrequency() {
        try {
            return frequency;
        }
        catch(Exception e) {
            return -1;
        }
    }

    public int getId() {
        try {
            return id;
        }
        catch(Exception e) {
            return -1;
        }
    }

    public String getCameraKey() {
        try {
            return camera_key;
        }
        catch(Exception e) {
            System.out.println("NO camera_key");
            return "null";
        }
    }
}
