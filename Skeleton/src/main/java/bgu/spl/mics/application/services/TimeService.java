package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.StatisticalFolder;

/**
 * TimeService acts as the global timer for the system, broadcasting TickBroadcast messages
 * at regular intervals and controlling the simulation's duration.
 */
public class TimeService extends MicroService {

    /**
     * Constructor for TimeService.
     *
     * @param TickTime  The duration of each tick in milliseconds.
     * @param Duration  The total number of ticks before the service terminates.
     */

    private final int tickTime; 
    private final int duration; 
    private StatisticalFolder statfolder = StatisticalFolder.getInstance();

    public TimeService(int TickTime, int Duration) {
        super("TimeService");
        tickTime = TickTime;
        duration = Duration;
    }

    /**
     * Initializes the TimeService.
     * Starts broadcasting TickBroadcast messages in a new thread and terminates after the specified duration.
     */
    @Override
    protected void initialize() {
        

        // Launch the ticking thread
        Thread tickingThread = new Thread(() -> {
            try {
                for (int currentTick = 1; currentTick <= duration; currentTick++) {
                    // Send a TickBroadcast
                    sendBroadcast(new TickBroadcast(currentTick));

                    // Increment time in statistics
                    statfolder.incrementRuntime();

                    // Wait tick time specified - tickTime is in seconds
                    Thread.sleep(tickTime*1000);
                }

                // Send a TerminateBroadcast after all ticks
                sendBroadcast(new TerminatedBroadcast());

                //System.out.println("TEST HEREEEEEEEEEEEEEEEEEEEE");

                // Signal this service to terminate
                terminate();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Preserve interrupt status
            }
        });

        // Subscribe to TerminateBroadcast with a lambda exp for the callback CHECK CALLBACK
        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast terminate) -> {
            System.out.println(getName() + " received termination signal. Shutting down.");
            tickingThread.interrupt();
            terminate();
        });

        // Handle CrashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast -> {
            System.err.println(getName() + " received crash notification: " + crashedBroadcast.getReason());
            tickingThread.interrupt();
            terminate();
        });

        tickingThread.start();

        System.out.println(getName() + " Initialized");
    }
}
