package bgu.spl.mics.application.objects;
import java.util.*;

/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */
public class GPSIMU {
    private int currentTick;
    private STATUS status;
    private List<Pose> poseList;

    public GPSIMU() {
        this.currentTick = 0;
        this.status = STATUS.UP;
        this.poseList = new ArrayList<>();
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