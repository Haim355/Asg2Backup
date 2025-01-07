package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */

public class Camera {
    private final int id;
    private int frequency;
    private STATUS status;
    private int counter;
    private List<StampedDetectedObjects> detectedObjects;
    private String ErrorMessage = "";
    private String camera_key;
    private StampedDetectedObjects next;


    public Camera(int id, int frequency) {
        this.id = id;
        this.frequency = frequency;
        counter = 0;
        STATUS status = STATUS.UP;
        detectedObjects = new ArrayList<>();
    }

    public void postDeserialize() {
        if (this.status == null) {
            this.status = STATUS.UP;
        }
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

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public List<StampedDetectedObjects> getDetectedObjects() {
        return detectedObjects;
    }

    public void setStampedDetectedObjects(List<StampedDetectedObjects> detectedObjects) {
        this.detectedObjects = detectedObjects;
        this.next = detectedObjects.get(counter);
    }
    private void CheckError(){
        setErrorMessage(next.checkForErrors());
        if(!getErrorMessage().equals("")){
            setStatus(STATUS.ERROR);
        }
    }
    public void set_camera_key(String key) {
        this.camera_key = key;
    }

    public String getCamera_key() {
        return camera_key;
    }

    public String getErrorMessage() {
        return ErrorMessage;
    }
    public int getCounter(){return counter;}
    public void setErrorMessage(String msg) {
        this.ErrorMessage = msg;
    }

    public String toString() {
        return "Camera ID:/n" + id + " Frequency:/n" + frequency + " Status:/n" + status + " Detected Objects:/n" + detectedObjects.toString();
    }

    public void addDetectedObject(StampedDetectedObjects obj) {
        detectedObjects.add(obj);
    }

    public StampedDetectedObjects getLastFrame(int time){
        StampedDetectedObjects toReturn = detectedObjects.get(0);
        for (StampedDetectedObjects detObj: detectedObjects){
            int currTime = detObj.getTime();
            if (currTime > time){break;}
            if ( currTime <= time && currTime > toReturn.getTime()){
                toReturn = detObj;
            }
        }
        return toReturn;
    }

    //   @return: the StampedDetectedObjects object which corresponds to @param time if exist else returns null
    //   @param time>=0
    //   @pre:  (getStatus() == STATUS.UP) && (count < getDetectedObject().size()) && (this.next != null)
    //   @post:  (this.status == UP && this.counter++) || (this.status == DOWN && next == null) || (this.status == ERROR && this.ErrorMessage != "")
    public StampedDetectedObjects getStampedByTime(int time) {
        if (time >= 0) {
            CheckError();
            StampedDetectedObjects curr = null;
            if ( next != null && time == next.getTime()) {
                if(getStatus() == STATUS.ERROR){
                    return next;
                }
                curr = next;
                if (counter + 1 < detectedObjects.size()) {
                    counter++;
                    next = detectedObjects.get(counter);
                } else {
                    next = null;
                    setStatus(STATUS.DOWN);
                }
            }
            return curr;
        }
        return null;
    }

    public StampedDetectedObjects getNext() {return next;}
    public void setCounter(int i) {counter = i;}
    public void setNext(StampedDetectedObjects obj) { next = obj;}
}


