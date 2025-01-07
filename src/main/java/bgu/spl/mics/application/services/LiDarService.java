package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.TrackedObject;

import java.util.List;

/**
 * LiDarService is responsible for processing data from the LiDAR sensor and
 * sending TrackedObjectsEvents to the FusionSLAM service.
 * <p>
 * This service interacts with the LiDarWorkerTracker object to retrieve and process
 * cloud point data and updates the system's StatisticalFolder upon sending its
 * observations.
 */
public class LiDarService extends MicroService {
    private final LiDarWorkerTracker liDarWorkerTracker;
    /**
     * Constructor for LiDarService.
     *
     * @param LiDarWorkerTracker A LiDAR Tracker worker object that this service will use to process data.
     */
    public LiDarService(LiDarWorkerTracker LiDarWorkerTracker) {
        super("Lidar Worker Id: " + LiDarWorkerTracker.getId());
        this.liDarWorkerTracker = LiDarWorkerTracker;
    }

    /**
     * Initializes the LiDarService.
     * Registers the service to handle DetectObjectsEvents and TickBroadcasts,
     * and sets up the necessary callbacks for processing data.
     */
    @Override
    protected void initialize() {
        subscribeBroadcast(TerminatedBroadcast.class, (broadcast) -> {
            if (broadcast.getSendermicro() instanceof TimeService){
                statistics.incrementTrackedObjects(liDarWorkerTracker.getNumberOfTrackedObject());
                terminate();
            }
        });
        subscribeBroadcast(CrahsedBroadCast.class, (broadcast) -> {
            System.out.println("firstPrint: " + liDarWorkerTracker.getNumberOfTrackedObject());
            statistics.incrementTrackedObjects(liDarWorkerTracker.getNumberOfTrackedObject());
            error.addLidarFrame(liDarWorkerTracker);
            terminate();
        });
        subscribeEvent(FalsePositiveEvent.class,
                (event)->{
                    liDarWorkerTracker.falsePositiveHandeling();
                });
        subscribeEvent(DetectObjectsEvent.class, (event) -> {
            liDarWorkerTracker.UpdateTrackedObject(event.getDetectedObject(), event.getTime());
            complete(event, true);
            if (liDarWorkerTracker.getStatus() == STATUS.ERROR) {
                error.setTime(event.getTime() - liDarWorkerTracker.getFrequency());
                error.setErrorMessage(this.liDarWorkerTracker.getErrorMessage());
                error.addFaultySensor(getName());
                error.addLidarFrame(liDarWorkerTracker);
                statistics.incrementTrackedObjects(liDarWorkerTracker.getNumberOfTrackedObject());
                sendBroadcast(new CrahsedBroadCast());
                terminate();
            } ///NOAM - the next 2 code block are identical shouldnt we add a private method for them?
            else if (liDarWorkerTracker.getStatus() == STATUS.UP) {
                List<List<TrackedObject>> readyitems = liDarWorkerTracker.ReadyItemsToSend(event.getTime());
                if (readyitems != null && !readyitems.isEmpty()) {
                    for (List<TrackedObject> list : readyitems) {
                       sendEvent(new TrackedObjectsEvent(this.getName(), list));
                    }
                }
            }
        });
        subscribeBroadcast(TickBroadcast.class, (broadcast) -> {
            if (liDarWorkerTracker.getStatus() == STATUS.UP) {
                List<List<TrackedObject>> readyitems = liDarWorkerTracker.ReadyItemsToSend(broadcast.getTickTime());
                if (readyitems != null && !readyitems.isEmpty()) {
                    for (List<TrackedObject> list : readyitems) {
                       sendEvent(new TrackedObjectsEvent(this.getName(), list));
                    }
                }
                liDarWorkerTracker.setState();
            }
            if (liDarWorkerTracker.getStatus() == STATUS.DOWN) {
                statistics.incrementTrackedObjects(liDarWorkerTracker.getNumberOfTrackedObject());
                sendBroadcast(new TerminatedBroadcast(this));
                terminate();
            }
        });
    }
}
