package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrahsedBroadCast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.STATUS;

/**
 * PoseService is responsible for maintaining the robot's current pose (position and orientation)
 * and broadcasting PoseEvents at every tick.
 */
public class PoseService extends MicroService {
    GPSIMU gpsimu;
    /**
     * Constructor for PoseService.
     *
     * @param gpsimu The GPSIMU object that provides the robot's pose data.
     */
    public PoseService(GPSIMU gpsimu) {
        super("PoseService");
        this.gpsimu = gpsimu;
    }

    /**
     * Initializes the PoseService.
     * Subscribes to TickBroadcast and sends PoseEvents at every tick based on the current pose.
     */
    @Override
    protected void initialize() {
        if(gpsimu.getStatus()== STATUS.UP){
        subscribeBroadcast(TickBroadcast.class, (broadcast) -> {
            Pose currentpose = gpsimu.getPoseByTime(broadcast.getTickTime());
            if(currentpose!=null) {
                sendEvent(new PoseEvent(this.getName(), currentpose, broadcast.getTickTime()));
            }
            if(gpsimu.getStatus() == STATUS.DOWN){
                sendBroadcast(new TerminatedBroadcast(this));
                terminate();
            }
        });
        }
        subscribeBroadcast(CrahsedBroadCast.class,(broadcast)->{terminate();
        });
    }
}
