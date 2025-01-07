package bgu.spl.mics.application.objects;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class LastFrames {
    private ConcurrentHashMap< Integer, StampedDetectedObjects> lastCameraFrames;
    private ConcurrentHashMap< Integer, List<TrackedObject>> lastLidarFrames;
    public LastFrames(){
      this.lastCameraFrames = new ConcurrentHashMap<>();
      this.lastLidarFrames = new ConcurrentHashMap<>();
    }
    public ConcurrentHashMap< Integer, StampedDetectedObjects> getLastCameraFrames(){
        return lastCameraFrames;
    }

    public ConcurrentHashMap< Integer, List<TrackedObject>> getLastLidarFrames(){
        return lastLidarFrames;
    }
    public void setLastCameraFrame(int id, StampedDetectedObjects detObj){
        lastCameraFrames.put(id , detObj);
    }

    public void setLastLidarFrame(int id, List<TrackedObject> obj){
        lastLidarFrames.put(id , obj);
    }

    @Override
    public String toString() {
        return toStringForCameras() +"/n"+"Lidar's frames:/n" + toStringForLidar();
    }

    public String toStringForCameras(){
        String str = "";
        for (StampedDetectedObjects detObj: lastCameraFrames.values()){
            str += "    " + detObj.toString() + "/n";
       }
        return str;
    }
    public String toStringForLidar() {
        String str = "";
        for (List<TrackedObject> l: lastLidarFrames.values()){
            str += "    " + l.toString() + "/n";
       }
        return str;
    }

}
