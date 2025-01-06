package bgu.spl.mics.application.objects;

import java.util.ArrayList;

/**
 * Represents an object tracked by the LiDAR.
 * This object includes information about the tracked object's ID, description,
 * time of tracking, and coordinates in the environment.
 */
public class TrackedObject {
    final private String id;
    private final int time;
    final private String description;
    private ArrayList<CloudPoint> coordinates; //Noam I CHANGED HERE to non final
    public TrackedObject(String id, int time, String description, ArrayList<CloudPoint> coordinates) {
        this.id = id;
        this.time = time;
        this.description = description;
        this.coordinates = coordinates;
    }
    public String getId() {
        return id;
    }
    public int getTime() {
        return time;
    }
    public String getDescription() {
        return description;
    }
    public ArrayList<CloudPoint> getCoordinates() {
        return coordinates;
    }
    public String toString() {
        return "TrackedObject [id=" + id + ", time=" + time + ", description=" + description + ", coordinates={/n" + getCoordinatesString() + "\n}]" ;
    }
    public String getCoordinatesString() {
        String str = "";
        for (CloudPoint point : coordinates) {
            str += point.toString() + "\n";
        }
        return str;
    }
}
