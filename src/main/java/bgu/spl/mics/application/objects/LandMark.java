package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a landmark in the environment map.
 * Landmarks are identified and updated by the FusionSlam service.
 */
public class LandMark {
private final String id;
private String description;
private List<CloudPoint> coordinates;
public LandMark(String id, String description) {
    this.id = id;
    this.description = description;
    coordinates = new ArrayList<CloudPoint>();
}
public LandMark(String id, String description, List<CloudPoint> coordinate) {
    this.id = id;
    this.description = description;
    this.coordinates = coordinate;
}
    public String getId() {return id;}
    public String getDescription() {return description;}
    public List<CloudPoint> getCoordinates() {return coordinates;}
    public void addCoordinate(CloudPoint point){coordinates.add(point);}
    public void setDescription(String description){this.description = description;}
    public void setCoordinates(List<CloudPoint> coordinates){this.coordinates = coordinates;}
    public void addCoordinates(CloudPoint point){this.coordinates.add(point);}
    public String toString(){
        return getId() + " " + getDescription() + " " + getCoordinates();
    }
}
