package bgu.spl.mics.application.objects;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Holds statistical information about the system's operation.
 * This class aggregates metrics such as the runtime of the system,
 * the number of objects detected and tracked, and the number of landmarks identified.
 */
public class StatisticalFolder {
    // runtime and landmarks are given from fusionSlamService and timeService
    // which are singletons therefore no need to synchronize
    private int runTime;
    private AtomicInteger numberOfDetectedObjects;
    private AtomicInteger numberOfTrackedObjects;
    private int numberOfLandmarks;

    private static class StatHolder{
        private static StatisticalFolder instance = new StatisticalFolder();
    }
    public StatisticalFolder() {
        this.runTime = 0;
        this.numberOfDetectedObjects = new AtomicInteger(0);
        this.numberOfTrackedObjects = new AtomicInteger(0);
        this.numberOfLandmarks = 0;
    }
    public static StatisticalFolder getInstance() {
        return StatHolder.instance;
    }
    //For unit tests only
    public void clearInstance(){
        StatHolder.instance.numberOfTrackedObjects = new AtomicInteger(0);
        StatHolder.instance.numberOfDetectedObjects = new AtomicInteger(0);
        StatHolder.instance.numberOfLandmarks = 0;
        StatHolder.instance.runTime = 0;
    }
    public int getRunTime() {return runTime;}
    public int getNumberOfDetectedObjects() {return numberOfDetectedObjects.get();}
    public int getNumberOfTrackedObjects() {return numberOfTrackedObjects.get();}
    public int getNumberOfLandmarks() {return numberOfLandmarks;}
    public void setRunTime(int runTime) {this.runTime = runTime;}
    public void setNumberOfDetectedObjects(int numberOfDetectedObjects) {
        int oldVal;
        do {
            oldVal = this.numberOfDetectedObjects.get();
        } while (!this.numberOfDetectedObjects.compareAndSet(oldVal, numberOfDetectedObjects));
    }
    public void setNumberOfTrackedObjects(int numberOfTrackedObjects) {
        int oldVal;
        do {
            oldVal = this.numberOfTrackedObjects.get();
        } while (!this.numberOfTrackedObjects.compareAndSet(oldVal, numberOfTrackedObjects));
    }
    public void setNumberOfLandmarks(int numberOfLandmarks) {this.numberOfLandmarks = numberOfLandmarks;}
    public void incrementRunTime(int time){this.runTime++;}
    public void incrementDetectedObjects(int toAdd){
        int newVal, oldVal;
        do {
            oldVal = this.numberOfDetectedObjects.get();
            newVal = oldVal + toAdd;
        } while (!this.numberOfDetectedObjects.compareAndSet(oldVal, newVal));
    }
    public void incrementTrackedObjects(int toAdd){
        int newVal, oldVal;
        do {
            oldVal = this.numberOfTrackedObjects.get();
            newVal = oldVal + toAdd;
        } while (!this.numberOfTrackedObjects.compareAndSet(oldVal, newVal));
    }

    public void incrementLandmarks(int toAdd){this.numberOfLandmarks += toAdd;}
    public String toString(){
        return  "Statistical Folder:\n" +
                "Run Time: " + this.runTime + "ms\n" +
                "Number of Detected Objects: " + this.numberOfDetectedObjects + "\n" +
                "Number of Tracked Objects: " + this.numberOfTrackedObjects + "\n" +
                "Number of Landmarks: " + this.numberOfLandmarks;
    }
}
