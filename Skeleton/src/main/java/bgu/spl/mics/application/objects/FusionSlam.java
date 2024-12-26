package bgu.spl.mics.application.objects;


import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam {
    // Singleton instance holder
    private static class FusionSlamHolder {
        private static FusionSlam INSTANCE = new FusionSlam();
    }
    
	private volatile LandMark[] landmarks;
    private volatile CopyOnWriteArrayList<Pose> previousPoses;
    private final ReadWriteLock lock;

	private FusionSlam(){   //private constructor for the singelton class
		landmarks = new LandMark[1000];
		lock = new ReentrantReadWriteLock();	
        previousPoses = new CopyOnWriteArrayList<Pose>();
	} 

	public static FusionSlam getInstance() {
        return FusionSlamHolder.INSTANCE;
    } 
}
