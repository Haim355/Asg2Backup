package bgu.spl.mics.application.objects;

/**
 * DetectedObject represents an object detected by the camera.
 * It contains information such as the object's ID and description.
 */
public class DetectedObject {
 private final String id;
 private final String description;
public DetectedObject(String _id, String _description){
    this.id = _id;
    this.description = _description;
    }
    public String getId(){
        return this.id;
    }
    public String getDescription(){
        return this.description;
    }
    @Override
    public String toString(){
        return "ID:" + this.id +"/n"+ "Description: " + this.description;
    }
}
