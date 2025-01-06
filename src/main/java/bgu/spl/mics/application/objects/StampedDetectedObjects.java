package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents objects detected by the camera at a specific timestamp.
 * Includes the time of detection and a list of detected objects.
 */
public class StampedDetectedObjects {
    private int time;
    private List<DetectedObject> detectedObjects;
    public StampedDetectedObjects(int _time){
        this.time = _time;
        this.detectedObjects = new ArrayList<DetectedObject>();
    }
    public StampedDetectedObjects(int _time, List<DetectedObject> _DetectedObjects){
        this.time = _time;
        this.detectedObjects =_DetectedObjects;
    }
    public int getTime(){
        return this.time;
    }
    public List<DetectedObject> getDetectedObjects(){
        return detectedObjects;
    }
    public void addDetectedObject(DetectedObject obj){
        detectedObjects.add(obj);
    }
    public String toString (){
        String res = "";
        for (DetectedObject obj: detectedObjects){
            res += obj.toString() + "\n";
        }
        return res;
    }
    public String checkForErrors(){
        for (DetectedObject obj: detectedObjects){
            if (obj.getId().equals("ERROR") || obj.getId().equals("error")){
                return obj.getDescription();
            }
        }
        return "";
    }

    public boolean isEmpty() {
        return detectedObjects.isEmpty();
    }
}
