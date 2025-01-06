package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

public class ERROR {
    private String message;
    private int time;
    private String faultySensor;
    private LastFrames lastFrames;
    private List<Pose> poses;



    private static class ErrorHolder{
        private static ERROR instance = new ERROR();
    }
    //For unit tests only
    public void clearInstance() {
        ErrorHolder.instance.setMessage("");
        ErrorHolder.instance.setTime(-1);
        ErrorHolder.instance.poses.clear();
        ErrorHolder.instance.setFaultySensor("");
        ErrorHolder.instance.setLastFrames(null);
    }

    public ERROR(){
       this.message = "";
       this.time = -1;
       this.faultySensor = "";
       this.lastFrames = new LastFrames();
       this.poses = new ArrayList<>();
    }

    public static ERROR getInstance(){
        return ErrorHolder.instance;
    }
    public String getErrorDescription() {
        return message;
    }
    public int getTime() {
        return time;
    }
    public String getFaultySensor() {
        return faultySensor;
    }
    public LastFrames getLastFrames() {
        return lastFrames;
    }
    public List<Pose> getPoses() {
        return poses;
    }
    public synchronized void setMessage(String message) {
       this.message = message;
    }
    public void setLastFrames(LastFrames lf){
        if (lf == null){lastFrames = new LastFrames();}
        else
            lastFrames = lf;
    }
    public void setFaultySensor(String s) {faultySensor = s;} public synchronized boolean setTime(int time) {
        if (this.time < time && !message.equals("")) {//means theres already an error
            return false;
        }
        this.time = time;
        return true;
    }
    public synchronized void addFaultySensor(String sensor) {
        if (faultySensor.equals("")) {
            faultySensor = sensor;
        }

    }
    public void setPoses(List<Pose> pose) {//NO SYNCHRO NEEDED SINCE THERE ONLY ONE FUSIONSLAM
        if (poses == null) {
            poses = new ArrayList<>();
        }//NEED TO VERIFY IF TO SUBTRACT
        for (Pose p : pose) {
            if (p.getTime() <= this.time)
                poses.add(p);
        }
    }
    public synchronized void addCameraFrame(Camera c){
        if (this.lastFrames == null){
            this.lastFrames = new LastFrames();
        } // NEED TO VERIFY IF TO SUBTRACT C.FREQUENCY
        StampedDetectedObjects lastFrame = null;
        if (!c.getErrorMessage().equals("")){//means this is the camera that caused the error
            int counter = c.getCounter();
            if (counter > 0) {
                lastFrame = c.getDetectedObjects().get(counter - 1);
            }
        }
        else {
            lastFrame = c.getLastFrame(time);
        }
        this.lastFrames.addCameraFrame(c.getId(), lastFrame);
    }
    public synchronized void addLidarFrame(LiDarWorkerTracker l){
        if (this.lastFrames == null){
            this.lastFrames = new LastFrames();
        } // NEED TO VERIFY IF TO SUBTRACT L.FREQUENCY
        this.lastFrames.addLidarFrame(l.getId(), l.getLastTrackedObjects());
    }




}
