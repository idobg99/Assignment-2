package bgu.spl.mics.application.objects;

import java.util.List;
import java.util.Map;

public class LidarsConfig {
    List<Map<String,Object>> LidarConfigurations;

    public LidarsConfig() {}

    public List<Map<String,Object>> getLidarConfigurations() {
        return LidarConfigurations;
    }
}
