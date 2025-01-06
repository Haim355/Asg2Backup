package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectObjectsEvents and generates TrackedObjectsEvents by using data from the LiDarDataBase.
 * Each worker tracks objects and sends observations to the FusionSlam service.
 */
public class LiDarWorkerTracker {
    final private int id;
    private final int frequency;
    private STATUS status;
    private List<TrackedObject> lastTrackedObjects;
    protected LiDarDataBase dataBase;
    private String ErrorMessage = "";
    private List<List<TrackedObject>> helditmes;
    private int numberOfTrackedObject = 0;
    public LiDarWorkerTracker(int id, int frequency) {
        this.id = id;
        this.frequency = frequency;
        status = STATUS.UP;
        lastTrackedObjects = new ArrayList<>();
    }

    public void postDeserialize() {
        status = STATUS.UP;
        this.helditmes = new ArrayList<>();
        lastTrackedObjects = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public int getFrequency() {
        return frequency;
    }

    public STATUS getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return ErrorMessage;
    }

    public int getNumberOfTrackedObject(){return numberOfTrackedObject;}

    public void setLidarDataBase(LiDarDataBase data) {
        this.dataBase = data;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public void setErrorMessage() {
        this.ErrorMessage = "LiDar Worker " + id + " disconnected";
        this.status = STATUS.ERROR;
    }

    public List<TrackedObject> getLastTrackedObjects() {
        return lastTrackedObjects;
    }

    public void addTrackedObject(TrackedObject obj) {
        lastTrackedObjects.add(obj);
    }

    public void addTrackedObjects(List<TrackedObject> trackedObjects) {
        helditmes.add(trackedObjects);
    }

    public void UpdateTrackedObject(List<DetectedObject> detObjs, int time) {
        List<StampedCloudPoints> cloudPoints = dataBase.getCloudPoints();
        List<TrackedObject> trackedObjs = new ArrayList<>();

        for (DetectedObject obj : detObjs) {
            ArrayList<CloudPoint> cpArr = new ArrayList<>(getCloudPointsByTime(cloudPoints, time, obj.getId()));
            if (status == STATUS.ERROR) {
                break;
            }
            TrackedObject temp = new TrackedObject(
                    obj.getId(),
                    time + frequency,
                    obj.getDescription(),
                    cpArr
            );
            trackedObjs.add(temp);
        }
        numberOfTrackedObject += trackedObjs.size();
        lastTrackedObjects = trackedObjs;
        helditmes.add(trackedObjs);
    }

    public List<List<TrackedObject>> ReadyItemsToSend(int time) {
        List<List<TrackedObject>> readyItems = new ArrayList<>();
        Iterator<List<TrackedObject>> iterator = helditmes.iterator();
        while (iterator.hasNext()) {
            List<TrackedObject> track = iterator.next();
            if (!track.isEmpty()) {
                int stampedTime = track.get(0).getTime();
                if (stampedTime <= time) {
                    readyItems.add(track);
                    for (TrackedObject obj : track) {
                        dataBase.increment();
                    }
                    iterator.remove();
                }
            }
        }
        return readyItems;
    }
    public void falsePositiveHandeling(){
        dataBase.increment();
    }

    private ArrayList<CloudPoint> getCloudPointsByTime(List<StampedCloudPoints> cloudPoints, int time, String id) { //HAIM I CHANGED HERE, CHANGE LOG -> IT RETURNS A LIST OF CLOUD POINTS
        for (StampedCloudPoints point : cloudPoints) {
            if (point.getTime() == time) {
                if (point.getId().equals("ERROR")) {
                    setErrorMessage();
                    break;
                }
                if (point.getId().equals(id)) {
                    return (ArrayList<CloudPoint>) point.getCloudPoints();
                }
            }
        }
        return null;
    }
    public void setState(){
        if (dataBase.flagState()){
            setStatus(STATUS.DOWN);
        }
    }

    public String toString() {
        String str = "";
        for (TrackedObject obj : lastTrackedObjects) {
            str += obj.toString() + "\n";
        }
        return "LiDarWorkerTracker [id=" + id + ", frequency=" + frequency + ", status=" + status + ", lastTrackedObjects=\n" + str + "]";
    }

}
