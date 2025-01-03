package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;

public class testEvent implements Event<String> {

    private final String string; // Robot's pose including position, orientation, and time

    /**
     * Constructs a PoseEvent with a given Pose object.
     *
     * @param pose The Pose object representing the robot's position and orientation.
     */
    public testEvent() {
        this.string = "";
    }

    @Override
    public String toString() {
        return "PoseEvent{" +
                "pose=" + string +
                '}';
    }
}


