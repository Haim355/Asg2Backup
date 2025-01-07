package bgu.spl.mics.parserClasses;

import bgu.spl.mics.application.objects.*;
import com.google.gson.*;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class ErrorOutputSerializer implements JsonSerializer<ErrorOutput> {

    @Override
    public JsonElement serialize(ErrorOutput errorOutput, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        // Serialize ERROR
        if (errorOutput.getError() != null) {
            ERROR error = errorOutput.getError();
            jsonObject.addProperty("error", error.getErrorDescription());
            jsonObject.addProperty("faultySensor", error.getFaultySensor());
            LastFrames lf = error.getLastFrames();
            JsonObject camerasFrames = new JsonObject();
            Map <Integer , StampedDetectedObjects> detMap = lf.getLastCameraFrames();
            for (int id: detMap.keySet()){
                JsonObject stObj = new JsonObject();
                StampedDetectedObjects temp = detMap.get(id);
                stObj.addProperty("time: ",temp.getTime());
                List<DetectedObject> detTemp = temp.getDetectedObjects();
                for (DetectedObject obj: detTemp){
                    JsonObject detObj = new JsonObject();
                    detObj.addProperty("id: ", obj.getId());
                    detObj.addProperty("description:", obj.getDescription());
                    stObj.add("",detObj);
                }
                camerasFrames.add("Camera" + id,stObj );
            }
            JsonObject lastFramesObject = new JsonObject();
            lastFramesObject.add("Cameras last frames: ",camerasFrames);

            JsonObject lidarsFrames = new JsonObject();
            Map <Integer , List<TrackedObject>> lidarsMap = lf.getLastLidarFrames();
            for (int id: lidarsMap.keySet()){
               JsonObject trackedList = new JsonObject();
                List<TrackedObject> temp = lidarsMap.get(id);
                for (TrackedObject obj: temp) {
                    JsonObject trackedObj = new JsonObject();
                    trackedObj.addProperty("id", obj.getId());
                    trackedObj.addProperty("time ", obj.getTime());
                    trackedObj.addProperty("description", obj.getDescription());
                    JsonObject jsonLstCp = new JsonObject();
                    List<CloudPoint> lstCp = obj.getCoordinates();
                    for (CloudPoint cp : lstCp) {
                        JsonObject cpObj = new JsonObject();
                        cpObj.addProperty("x", cp.getX());
                        cpObj.addProperty("y", cp.getY());
                        jsonLstCp.add("", cpObj);
                    }
                    trackedObj.add("coordinates", jsonLstCp);
                    trackedList.add("",trackedObj);
                }
                 lidarsFrames.add("LiDarTrackerWorker" + id, trackedList );
            }
            lastFramesObject.add("LiDar Workers last frames:", lidarsFrames);
            jsonObject.add("Last Frames: " , lastFramesObject);
            JsonObject posesObject = new JsonObject();
            List <Pose> poses = error.getPoses();
            for (Pose p: poses){
                JsonObject poseObject = new JsonObject();
                poseObject.addProperty("time:", p.getTime());
                poseObject.addProperty("x:", p.getX());
                poseObject.addProperty("y:", p.getY());
                poseObject.addProperty("yaw:", p.getYaw());
                posesObject.add("",poseObject);
            }
            jsonObject.add("Poses:" , posesObject);
        }

        // Serialize StatisticalFolder
        if (errorOutput.getStats() != null) {
            JsonObject statsObject = new JsonObject();
            StatisticalFolder stats = errorOutput.getStats();
            statsObject.addProperty("runtime:", stats.getRunTime());
            statsObject.addProperty("numberOfDetectedObjects:", stats.getNumberOfDetectedObjects());
            statsObject.addProperty("numberOfTrackedObjects:", stats.getNumberOfTrackedObjects());
            statsObject.addProperty("numberOfLandmarks:", stats.getNumberOfLandmarks());
            jsonObject.add("stats:", statsObject);
        }

        // Serialize LandMarks
        if (errorOutput.getLandMarks() != null) {
            JsonObject landmarksObject = new JsonObject();
            Map<String, LandMark> landmarks = errorOutput.getLandMarks();
            for (Map.Entry<String, LandMark> entry : landmarks.entrySet()) {
                String key = entry.getKey();
                LandMark landMark = entry.getValue();

                JsonObject landMarkObject = new JsonObject();
                landMarkObject.addProperty("id:", landMark.getId());
                landMarkObject.addProperty("description:", landMark.getDescription());

                JsonArray coordinatesArray = new JsonArray();
                landMark.getCoordinates().forEach(coordinate -> {
                    JsonObject coordinateObject = new JsonObject();
                    coordinateObject.addProperty("x:", coordinate.getX());
                    coordinateObject.addProperty("y:", coordinate.getY());
                    coordinatesArray.add(coordinateObject);
                });

                landMarkObject.add("coordinates:", coordinatesArray);
                landmarksObject.add(key, landMarkObject);
            }
            jsonObject.add("landMarks", landmarksObject);
        }

        return jsonObject;
    }
}

