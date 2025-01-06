package bgu.spl.mics.application.objects;

import bgu.spl.mics.MessageBusImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam {
    private final ConcurrentHashMap<String, LandMark> landMarks;
    private final List<Pose> Poses;
    private ConcurrentHashMap<Integer, Pose> poseMap;
    private List<LandMark> pending;
    private AtomicInteger RunningServices;


    private static class Fusionslamholder {
        private static FusionSlam instance = new FusionSlam();
    }

    private FusionSlam() {
        landMarks = new ConcurrentHashMap<>();
        Poses = new ArrayList<>();
        poseMap = new ConcurrentHashMap<>();
        pending = new ArrayList<>();

    }

    public ConcurrentHashMap<String, LandMark> getLandMarks() {
        return landMarks;
    }

    public int getMapSize() {
        return landMarks.size();
    }

    public List<Pose> getPoses() {
        return Poses;
    }

    public void addPose(Pose pose) {
        Poses.add(pose);
        poseMap.put(pose.getTime(), pose);
        for (LandMark land : pending) {
            addLandMark(land.getId(), land.getDescription(), land.getCoordinates(), pose.getTime());
        }
        pending.clear();
    }
    public List<LandMark> getPending() {return pending;}
    public Pose getPosebyTime(int time) {
        return poseMap.get(time);
    }

    /*
    @param: timeOfDiscover >= 0 && id.length()>0 && coordinate!=null
    @pre:  ?????
    @post: pre(pending.size)+1 == post(pending.size)
    @post: pre(landMarks.size) == post(landMarks.size)
    @post: pre(landMarks.size)+1 == post(landMarks.size)

     */
    public void addLandMark(String id, String description, List<CloudPoint> coordinate, int TimeOfDiscover) {
        Pose pose = getPosebyTime(TimeOfDiscover);
        if (pose != null) {
            List<CloudPoint> newCoords = Convertor(coordinate, pose);
            if (landMarks.containsKey(id)) {
                LandMark oldLandMark = landMarks.get(id);
                List<CloudPoint> averagedCoords = averageCoordinates(oldLandMark.getCoordinates(), newCoords);
                oldLandMark.setCoordinates(averagedCoords);  // Avoid double conversion
            } else {
                LandMark newLandMark = new LandMark(id, description, newCoords);
                landMarks.put(id, newLandMark);
            }
        } else {
            pending.add(new LandMark(id, description, coordinate));
        }
    }
    public List<CloudPoint> Convertor(List<CloudPoint> cord, Pose pose) {
        List<CloudPoint> newCord = new ArrayList<>();
        float rotation = pose.getYaw() * (float) Math.PI / 180;  // Convert to radians
        float rotationcos = (float) Math.cos(rotation);
        float rotationsin = (float) Math.sin(rotation);

        for (CloudPoint c : cord) {
            float xglobal = (float) (rotationcos * c.getX() - rotationsin * c.getY() + pose.getX());
            float yglobal = (float) (rotationsin * c.getX() + rotationcos * c.getY() + pose.getY());

            CloudPoint newCoords = new CloudPoint(xglobal, yglobal);
            newCord.add(newCoords);
        }
        return newCord;
    }
    public List<CloudPoint> averageCoordinates(List<CloudPoint> oldCoords, List<CloudPoint> newCoords) {
        List<CloudPoint> averagedCoords = new ArrayList<>();
        int size = Math.min(oldCoords.size(), newCoords.size());

        for (int i = 0; i < size; i++) {
            float avgX = (float) ((oldCoords.get(i).getX() + newCoords.get(i).getX()) / 2);
            float avgY = (float) ((oldCoords.get(i).getY() + newCoords.get(i).getY()) / 2);
            averagedCoords.add(new CloudPoint(avgX, avgY));
        }
        if (oldCoords.size() > newCoords.size()) {
            averagedCoords.addAll(oldCoords.subList(size, oldCoords.size()));
        } else {
            averagedCoords.addAll(newCoords.subList(size, newCoords.size()));
        }

        return averagedCoords;
    }
    public void setNumberOfNumberOfServices(int size) {
        RunningServices = new AtomicInteger(size);
    }

    public int getRunningServices() {
        return RunningServices.get();
    }

    public void decreaseNumber() {
        int prev;
        do {
            prev = RunningServices.get();
        } while (!RunningServices.compareAndSet(prev, prev - 1));
    }

    //Only for unit testing
    public void clearInstance() {
        synchronized (Fusionslamholder.instance) {
            Fusionslamholder.instance.landMarks.clear();
            Fusionslamholder.instance.Poses.clear();
            Fusionslamholder.instance.pending.clear();
            Fusionslamholder.instance.poseMap.clear();
            Fusionslamholder.instance.RunningServices = new AtomicInteger(0);
        }

    }
    public static FusionSlam getInstance() {
        return Fusionslamholder.instance;
    }

}
