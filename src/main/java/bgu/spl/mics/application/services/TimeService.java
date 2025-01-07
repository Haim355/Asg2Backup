package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrahsedBroadCast;
import bgu.spl.mics.application.messages.KillTimeEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.STATUS;

/**
 * TimeService acts as the global timer for the system, broadcasting TickBroadcast messages
 * at regular intervals and controlling the simulation's duration.
 */
public class TimeService extends MicroService {
    private final int TickTime;
    private final int Duration;
    private int currentTick;
    private STATUS status;

    /**
     * Constructor for TimeService.
     *
     * @param TickTime The duration of each tick in milliseconds.
     * @param Duration The total number of ticks before the service terminates.
     */
    public TimeService(int TickTime, int Duration) {
        super("TickTime: " + TickTime + " Duration: " + Duration + "");
        this.TickTime = TickTime;
        this.Duration = Duration;
        this.currentTick = 0;
        this.status = STATUS.UP;
    }
    //For unit test only
    public int getCurrentTick(){return currentTick;}
    /**
     * Initializes the TimeService.
     * Starts broadcasting TickBroadcast messages and terminates after the specified duration.
     */
    @Override
    protected void initialize() {
        subscribeEvent(KillTimeEvent.class, (event) -> {
            status = STATUS.DOWN;
            statistics.setRunTime(currentTick);
            terminate();
        });
        subscribeBroadcast(CrahsedBroadCast.class, (broadcast) -> {
            status = STATUS.DOWN;
            statistics.setRunTime(currentTick);
            terminate();
        });
        subscribeBroadcast(TickBroadcast.class, (broadcast) -> {
            if (currentTick < Duration && status == STATUS.UP) {
                try {
                    Thread.sleep(TickTime*1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                currentTick++;
                sendBroadcast(new TickBroadcast(currentTick));

            } else {
                sendBroadcast(new TerminatedBroadcast(this));
                status = STATUS.DOWN;
                statistics.setRunTime(currentTick);
                terminate();
            }
        });
        sendBroadcast(new TickBroadcast(currentTick));
    }
}
