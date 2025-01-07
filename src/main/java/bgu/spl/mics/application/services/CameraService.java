package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

/**
 * CameraService is responsible for processing data from the camera and
 * sending DetectObjectsEvents to LiDAR workers.
 * <p>
 * This service interacts with the Camera object to detect objects and updates
 * the system's StatisticalFolder upon sending its observations.
 */
public class CameraService extends MicroService {
    private Camera camera;
    private int timestamps;
    private int numberofsentitems;

    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public CameraService(Camera camera) {
        super("Camera Id: " + camera.getId());
        this.camera = camera;
        this.timestamps = 0;
        this.numberofsentitems=0;
    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for sending
     * DetectObjectsEvents.
     */
    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class
                , (broadcast) -> {

                    if (camera.getStatus() == STATUS.UP) {
                        StampedDetectedObjects detList = camera.getStampedByTime(broadcast.getTickTime() - camera.getFrequency());
                      if (camera.getStatus() == STATUS.ERROR) {
                          error.setTime(broadcast.getTickTime());
                          error.setErrorMessage(camera.getErrorMessage());
                          error.addFaultySensor(getName());
                          error.addCameraFrame(camera);
                          statistics.incrementDetectedObjects(numberofsentitems);
                          sendBroadcast(new CrahsedBroadCast());
                          terminate();
                        }
                        if (detList != null && !detList.isEmpty()) {
                            numberofsentitems += detList.getDetectedObjects().size();
                            sendEvent(new DetectObjectsEvent(this.getName(), detList.getDetectedObjects(), broadcast.getTickTime() - camera.getFrequency()));
                        }
                        if(detList != null) {
                            if (detList.isEmpty()) {
                                sendEvent(new FalsePositiveEvent(this.getName()));
                            }
                        }
                    } else {
                        statistics.incrementDetectedObjects(numberofsentitems);
                        sendBroadcast(new TerminatedBroadcast(this));
                        terminate();
                    }
                });
        subscribeBroadcast(TerminatedBroadcast.class, (broadcast) -> {
            if (broadcast.getSendermicro() instanceof TimeService){
                statistics.incrementDetectedObjects(numberofsentitems);
                terminate();
            }
        });
        subscribeBroadcast(CrahsedBroadCast.class, (broadcast) -> {
            statistics.incrementDetectedObjects(numberofsentitems);
            error.addCameraFrame(camera);
            terminate();
        });

    }
}
