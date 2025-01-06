package bgu.spl.mics.parserClasses;
import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.LandMark;
import bgu.spl.mics.parserClasses.OutputData;
import com.google.gson.*;
import java.lang.reflect.Type;
import java.util.Map;

public class OutputSerializer implements JsonSerializer<OutputData> {
    @Override
    public JsonElement serialize(OutputData src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject root = new JsonObject();

        // Add statistics in a single line
        // Add statistics in a single line
        root.addProperty("systemRuntime", src.getSystemRuntime());
        root.addProperty("numDetectedObjects", src.getNumDetectedObjects());
        root.addProperty("numTrackedObjects", src.getNumTrackedObjects());
        root.addProperty("numLandmarks", src.getNumLandmarks());


        // Add landmarks
        JsonObject landmarksObject = new JsonObject();
        for (Map.Entry<String, LandMark> entry : src.getLandMarks().entrySet()) {
            JsonObject landmarkJson = new JsonObject();
            landmarkJson.addProperty("id", entry.getValue().getId());
            landmarkJson.addProperty("description", entry.getValue().getDescription());

            JsonArray coordinatesArray = new JsonArray();
            for (CloudPoint point : entry.getValue().getCoordinates()) {
                JsonObject pointJson = new JsonObject();
                pointJson.addProperty("x", point.getX());
                pointJson.addProperty("y", point.getY());
                coordinatesArray.add(pointJson);
            }

            landmarkJson.add("coordinates", coordinatesArray);
            landmarksObject.add(entry.getKey(), landmarkJson);
        }
        root.add("landMarks", landmarksObject);

        return root;
    }
}
