package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.LandMark;
import bgu.spl.mics.application.objects.LiDarDataBase;
import bgu.spl.mics.application.objects.TrackedObject;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FusionSlamService integrates data from multiple sensors to build and update
 * the robot's global map.
 * <p>
 * This service receives TrackedObjectsEvents from LiDAR workers and PoseEvents from the PoseService,
 * transforming and updating the map with new landmarks.
 */
public class FusionSlamService extends MicroService {
    FusionSlam fusionSlam;

    /**
     * Constructor for FusionSlamService.
     *
     * @param fusionSlam The FusionSLAM object responsible for managing the global map.
     */
    public FusionSlamService(FusionSlam fusionSlam) {
        super("FusionSlamService");
        this.fusionSlam = fusionSlam.getInstance();
    }

    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle TrackedObjectsEvents, PoseEvents, and TickBroadcasts,
     * and sets up callbacks for updating the global map.
     */
    @Override
    protected void initialize() {
        subscribeEvent(PoseEvent.class, (event) -> {
            fusionSlam.addPose(event.getPose());
        });
        subscribeEvent(TrackedObjectsEvent.class, (event) -> {
            event.getTrackedObjectList().forEach(trackedObject -> {
                fusionSlam.addLandMark(trackedObject.getId(), trackedObject.getDescription(), trackedObject.getCoordinates(), trackedObject.getTime());
            });
    });
        subscribeBroadcast(TerminatedBroadcast.class, ((broadcast) -> {
            if (broadcast.getSendermicro() instanceof CameraService ||broadcast.getSendermicro() instanceof PoseService || broadcast.getSendermicro() instanceof LiDarService ) {
                fusionSlam.decreaseNumber();
                System.out.println("Ended " + broadcast.getSendermicro().getName());
                if (fusionSlam.getRunningServices() == 0) {
                    statistics.setNumberOfLandmarks(fusionSlam.getLandMarks().size());
                    sendEvent(new KillTimeEvent(this.getName()));
                    System.out.println("Number of objects " + fusionSlam.getLandMarks().size());
                    for (LandMark landMark: fusionSlam.getLandMarks().values()){
                        System.out.println(landMark.toString());
                    }
                    terminate();
                }
            }
            else{
               terminate();
            }
        }));
            subscribeBroadcast(CrahsedBroadCast.class, (broadCast) -> {
                statistics.setNumberOfLandmarks(fusionSlam.getLandMarks().size());
                error.setPoses(fusionSlam.getPoses());
                terminate();
            });
    }
}
