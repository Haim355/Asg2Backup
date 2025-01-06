package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a group of cloud points corresponding to a specific timestamp.
 * Used by the LiDAR system to store and process point cloud data for tracked objects.
 */
public class StampedCloudPoints {
    final private String id;
    final private int time;
    private List<CloudPoint> cloudPoints;
    public StampedCloudPoints(String id, int time) {
        this.id = id;
        this.time = time;
        this.cloudPoints = new ArrayList<CloudPoint>();
    }
    public String toString() {
        String str = "";
        for (CloudPoint point : cloudPoints) {
            str += point.toString() + "\n";
        }
        return "StampedCloudPoints [id=" + id + ", time=" + time + ", cloudPoints=\n" + str + "]";
    }
    public String getId() {
        return id;
    }
    public int getTime() {
        return time;
    }
    public List<CloudPoint> getCloudPoints() {
        return cloudPoints;
    }
    public void addCloudPoint(CloudPoint point){
        cloudPoints.add(point);
    }

}
