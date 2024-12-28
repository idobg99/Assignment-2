package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

/**
 * Broadcast representing a crash notification in the system.
 */
public class CrashedBroadcast implements Broadcast {
    private final String reason;

    /**
     * Constructor for CrashedBroadcast.
     *
     * @param reason The reason for the crash.
     */
    public CrashedBroadcast(String reason) {
        this.reason = reason;
    }

    /**
     * Retrieves the reason for the crash.
     *
     * @return The reason for the crash.
     */
    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return "CrashedBroadcast{reason='" + reason + "'}";
    }
}
