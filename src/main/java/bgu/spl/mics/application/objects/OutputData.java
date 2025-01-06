package bgu.spl.mics.application.objects;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class OutputData {
    private int runtime;
    private int numberOfDetectedObjects;
    private int numberOfTrackedObjects;
    private int numberOfLandmarks;
    private ConcurrentHashMap<String, LandMark> landmarks;
    private static class OutputDataHolder{
        private static OutputData instance = new OutputData();
    }
    public static OutputData getInstance() {
        return OutputDataHolder.instance;
    }
    public int getRuntime() {return runtime;}
    public int getNumberOfDetectedObjects() {return numberOfDetectedObjects;}
    public int getNumberOfTrackedObjects() {return numberOfTrackedObjects;}
    public int getNumberOfLandmarks() {return numberOfLandmarks;}
    public ConcurrentHashMap<String , LandMark> getLandmarks(){return landmarks;}
    public void setRuntime(int runTime) {this.runtime = runTime;}
    public void setNumDetectedObjects(int numberOfDetectedObjects) {this.numberOfDetectedObjects = numberOfDetectedObjects;}
    public void setNumTrackedObjects(int numberOfTrackedObjects) {this.numberOfTrackedObjects = numberOfTrackedObjects;}
    public void setNumLandmarks(int numberOfLandmarks) {this.numberOfLandmarks = numberOfLandmarks;}
    public void setLandmarks(ConcurrentHashMap<String, LandMark> land){landmarks = land;}

}
