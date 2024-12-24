package bgu.spl.mics.application.objects;

import java.util.*;
import java.util.concurrent.locks.*;

/**
 * Holds statistical information about the system's operation.
 * This class aggregates metrics such as the runtime of the system,
 * the number of objects detected and tracked, and the number of landmarks identified.
 */
public class StatisticalFolder {
    public static int systemRuntime = 0;
    public static int numDetectedObjects = 0;
    public static int numTrackedObjects = 0;
    public static int numLandmarks = 0;
    public static final ReentrantLock lock = new ReentrantLock();

    public static void incrementRuntime() {
        lock.lock();
        try {
            systemRuntime++;
        } finally {
            lock.unlock();
        }
    }

    public static void incrementDetectedObjects(int count) {
        lock.lock();
        try {
            numDetectedObjects += count;
        } finally {
            lock.unlock();
        }
    }

    public static void incrementTrackedObjects(int count) {
        lock.lock();
        try {
            numTrackedObjects += count;
        } finally {
            lock.unlock();
        }
    }

    public static void incrementLandmarks() {
        lock.lock();
        try {
            numLandmarks++;
        } finally {
            lock.unlock();
        }
    }

}