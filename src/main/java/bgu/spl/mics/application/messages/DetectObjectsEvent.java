package bgu.spl.mics.application.messages;
import bgu.spl.mics.EventImp;
import bgu.spl.mics.application.objects.DetectedObject;
//import javafx.util.Pair;

import java.util.List;

public class DetectObjectsEvent extends EventImp<Boolean> {
    private List<DetectedObject> detectedObject;
    private int time;

    public DetectObjectsEvent(String SenderId, List<DetectedObject> detectedObject, int time) {
        super(SenderId);
        this.detectedObject = detectedObject;
        this.time = time;

    }

    public List<DetectedObject> getDetectedObject() {
        return detectedObject;
    }
    public int getTime() {
        return time;
    }
    /**The Flow here should be: that we create this event in camera(MIcroservice?) -> send it using the Camera service -> handle it via the Lidar
     * -> when the event is over just set the Future to True (thats why the event T value is boolean </> **/
}
