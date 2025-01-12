package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

public class ERROR {
    private String ErrorMessage;
    private int systemRunTime;
    private String faultySensor;
    private LastFrames lastFrames;
    private List<Pose> Poses;



    private static class ErrorHolder{
        private static ERROR instance = new ERROR();
    }
    //For unit tests only
    public void clearInstance() {
        ErrorHolder.instance.setErrorMessage("");
        ErrorHolder.instance.setTime(-1);
        ErrorHolder.instance.Poses.clear();
        ErrorHolder.instance.setFaultySensor("");
        ErrorHolder.instance.setLastFrames(null);
    }

    public ERROR(){
       this.ErrorMessage = "";
       this.systemRunTime = -1;
       this.faultySensor = "";
       this.lastFrames = new LastFrames();
       this.Poses = new ArrayList<>();
    }

    public static ERROR getInstance(){
        return ErrorHolder.instance;
    }
    public String getErrorDescription() {
        return ErrorMessage;
    }
    public int getSystemRunTime() {
        return systemRunTime;
    }
    public String getFaultySensor() {
        return faultySensor;
    }
    public LastFrames getLastFrames() {
        return lastFrames;
    }
    public List<Pose> getPoses() {
        return Poses;
    }
    public void setErrorMessage(String errorMessage) {
       this.ErrorMessage = errorMessage;
    }
    public void setLastFrames(LastFrames lf){
        if (lf == null){
            lastFrames = new LastFrames();
        }
        else{
            lastFrames = lf;
        }
    }
    public void setFaultySensor(String s) {
        faultySensor = s;
    }
    public void setTime(int time) {
        this.systemRunTime = time;
    }
    public synchronized void addFaultySensor(String sensor) {
        if (faultySensor.equals("")) {
            faultySensor = sensor;
        }

    }
    public void setPoses(List<Pose> pose) {
        if (Poses == null) {
            Poses = new ArrayList<>();
        }//NEED TO VERIFY IF TO SUBTRACT
        for (Pose p : pose) {
            if (p.getTime() <= this.systemRunTime)
                Poses.add(p);
        }
    }
    public synchronized void addCameraFrame(Camera c){
        if (this.lastFrames == null){
            this.lastFrames = new LastFrames();
        }
        StampedDetectedObjects lastFrame = null;
        if (!c.getErrorMessage().equals("")){
            int counter = c.getCounter();
            if (counter > 0) {
                lastFrame = c.getDetectedObjects().get(counter - 1);
            }
        }
        else {
            lastFrame = c.getLastFrame(systemRunTime);
        }
        this.lastFrames.setLastCameraFrame(c.getId(), lastFrame);
    }
    public synchronized void addLidarFrame(LiDarWorkerTracker l){
        if (this.lastFrames == null){
            this.lastFrames = new LastFrames();
        }
        this.lastFrames.setLastLidarFrame(l.getId(), l.getLastTrackedObjects());
    }




}
