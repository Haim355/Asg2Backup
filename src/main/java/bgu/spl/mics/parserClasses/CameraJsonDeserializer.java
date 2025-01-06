package bgu.spl.mics.parserClasses;

import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class CameraJsonDeserializer implements JsonDeserializer<Camera> {
        @Override
        public Camera deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
                try {
                        JsonObject jsonObject = json.getAsJsonObject();
                        int id = jsonObject.get("id").getAsInt();
                        int frequency = jsonObject.get("frequency").getAsInt();

                         Camera camera = new Camera(id, frequency);

                // NOAM - in this kind of implementation, is the field
                // ArrayList<StampedDetectedObject> detectedObjects of every camera is built correctly
                        //
                        if (jsonObject.has("detectedObjects")) {
                                Type listType = new TypeToken<List<StampedDetectedObjects>>(){}.getType();
                                List<StampedDetectedObjects> detectedObjects = context.deserialize(jsonObject.get("detectedObjects"), listType);
                                if (detectedObjects != null) {
                                        camera.setStampedDetectedObjects(detectedObjects);
                                }
                        }

                        return camera;

                } catch (Exception e) {
                        throw new JsonParseException("Error parsing Camera: " + e.getMessage());
                }
        }
}
