package bgu.spl.mics.parserClasses;

import bgu.spl.mics.application.objects.LandMark;

import java.util.Map;

public class OutputData {
    private int systemRuntime; // Updated field name
    private int numDetectedObjects; // Updated field name
    private int numTrackedObjects; // Updated field name
    private int numLandmarks; // Updated field name
    private Map<String, LandMark> landMarks; // Updated field name and type

    private static class OutputDataHolder {
        private static final OutputData instance = new OutputData();
    }

    public static OutputData getInstance() {
        return OutputDataHolder.instance;
    }

    // Getters
    public int getSystemRuntime() {
        return systemRuntime;
    }

    public int getNumDetectedObjects() {
        return numDetectedObjects;
    }

    public int getNumTrackedObjects() {
        return numTrackedObjects;
    }

    public int getNumLandmarks() {
        return numLandmarks;
    }

    public Map<String, LandMark> getLandMarks() {
        return landMarks;
    }

    // Setters
    public void setSystemRuntime(int systemRuntime) {
        this.systemRuntime = systemRuntime;
    }

    public void setNumDetectedObjects(int numDetectedObjects) {
        this.numDetectedObjects = numDetectedObjects;
    }

    public void setNumTrackedObjects(int numTrackedObjects) {
        this.numTrackedObjects = numTrackedObjects;
    }

    public void setNumLandmarks(int numLandmarks) {
        this.numLandmarks = numLandmarks;
    }

    public void setLandMarks(Map<String, LandMark> landMarks) {
        this.landMarks = landMarks;
    }
}
