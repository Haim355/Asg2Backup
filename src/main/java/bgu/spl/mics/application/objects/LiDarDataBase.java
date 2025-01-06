package bgu.spl.mics.application.objects;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {
//    private static volatile LiDarDataBase instance;
    private List<StampedCloudPoints> cloudPoints; 
    private int ObjectsSize;
    private String filePath;
    private AtomicInteger NumberOfReturns;
    private volatile Boolean flag;

    private static class LidarDataBaseHolder{
        private static LiDarDataBase instance = new LiDarDataBase();
    }
    // Private constructor for singleton
    private LiDarDataBase() {
        this.cloudPoints = new ArrayList<>();
        this.NumberOfReturns = new AtomicInteger(0);
    }
    public void fullSet(String filePath) {
        this.filePath = filePath;
        loadData();
        this.ObjectsSize = cloudPoints.size();
        flag = false;
    }
    /**
     * Returns the singleton instance of LiDarDataBase.
     *
     * @param filePath The path to the LiDAR data file.
     * @return The singleton instance of LiDarDataBase.
     */
    public static LiDarDataBase getInstance(String filePath) {
        LiDarDataBase instance = LidarDataBaseHolder.instance;
        instance.fullSet(filePath);
        return instance;//Changed it ! need to verify
    }

    private void loadData() {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(filePath)) {
            Type listType = new TypeToken<ArrayList<JsonObject>>() {}.getType();
            List<JsonObject> rawData = gson.fromJson(reader, listType);

            cloudPoints.clear();

            for (JsonObject obj : rawData) {
                String id = obj.get("id").getAsString();
                int time = obj.get("time").getAsInt();
                StampedCloudPoints stamped = new StampedCloudPoints(id, time);

                JsonArray points = obj.getAsJsonArray("cloudPoints");
                for (JsonElement point : points) {
                    JsonArray coordinates = point.getAsJsonArray();
                    double x = coordinates.get(0).getAsDouble();
                    double y = coordinates.get(1).getAsDouble();
                    stamped.addCloudPoint(new CloudPoint(x, y));
                }
                cloudPoints.add(stamped);
            }
            System.out.println("LiDAR data loaded successfully. Points: " + cloudPoints.size());
        } catch (IOException e) {
            System.err.println("Failed to load LiDAR data from: " + filePath);
            e.printStackTrace();
        }
    }
    public void increment(){
        NumberOfReturns.incrementAndGet();
        endingSenrio();
    }
    public int getNumberOfReturns(){
        return NumberOfReturns.get();
    }
    private void endingSenrio(){
        if(getNumberOfReturns()==ObjectsSize){
            flag = true;
        }
    }
    public boolean flagState(){
        return flag;
    }

    public List<StampedCloudPoints> getCloudPoints() {
        return cloudPoints;
    }
}
