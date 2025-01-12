package bgu.spl.mics.application;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
import bgu.spl.mics.parserClasses.OutputData;
import bgu.spl.mics.parserClasses.OutputSerializer;
import com.google.gson.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * The main entry point for the GurionRock Pro Max Ultra Over 9000 simulation.
 * <p>
 * This class initializes the system and starts the simulation by setting up
 * services, objects, and configurations.
 * </p>
 */
public class GurionRockRunner {

    public static void main(String[] args) {
        int numberofservices = 3;
        if (args.length == 0) {
            System.err.println("Configuration file path must be provided as an argument.");
            return;
        }

        Gson gson = new Gson();
        JsonObject config;

        try (FileReader configFileReader = new FileReader(args[0])) {
            config = gson.fromJson(configFileReader, JsonObject.class);
        } catch (FileNotFoundException e) {
            System.err.println("Configuration file not found: " + e.getMessage());
            return;
        } catch (IOException e) {
            System.err.println("Error reading configuration file: " + e.getMessage());
            return;
        }

        if (config == null) {
            System.err.println("Failed to parse configuration file.");
            return;
        }
        int lastSepratorIndex = args[0].lastIndexOf('/');
        String dirPathString = args[0].substring(0, lastSepratorIndex);
        //*********************** Cameras ******************************//
        JsonObject cams = config.getAsJsonObject("Cameras");
        if (cams == null) {
            System.err.println("Camera configuration is missing.");
            return;
        }

        String cameraDataPath = cams.get("camera_datas_path").getAsString().substring(1) ;
        JsonArray cameraConfigs = cams.getAsJsonArray("CamerasConfigurations");
        if (cameraConfigs == null) {
            System.err.println("Camera configurations are missing.");
            return;
        }

        List<Camera> cameraObjects = new ArrayList<>();
        List<CameraService> cameraServices = new ArrayList<>();

        try (FileReader camDataReader = new FileReader(dirPathString+cameraDataPath)) {
            JsonObject camData = gson.fromJson(camDataReader, JsonObject.class);

            for (JsonElement element : cameraConfigs) {
                Camera camera = gson.fromJson(element, Camera.class);
                camera.postDeserialize();
                JsonArray currentJson = camData.getAsJsonArray(camera.getCamera_key());

                if (currentJson != null) {
                    List<StampedDetectedObjects> stampedList = new ArrayList<>();
                    for (JsonElement jsonElement : currentJson) {
                        StampedDetectedObjects stampedDetected = gson.fromJson(jsonElement, StampedDetectedObjects.class);
                        stampedList.add(stampedDetected);
                    }
                    camera.setStampedDetectedObjects(stampedList);
                }
                cameraObjects.add(camera);
                cameraServices.add(new CameraService(camera));
                numberofservices++;
            }
        } catch (FileNotFoundException e) {
            System.err.println("Camera data file not found: " + e.getMessage());
            return;
        } catch (IOException e) {
            System.err.println("Error reading camera data: " + e.getMessage());
            return;
        }

        //*********************** LiDar Workers ******************************//
        JsonObject liDarWorkers = config.getAsJsonObject("LiDarWorkers");
        if (liDarWorkers == null) {
            System.err.println("LiDar worker configuration is missing.");
            return;
        }

        String lidarDataPath = liDarWorkers.get("lidars_data_path").getAsString().substring(1);
        LiDarDataBase lidarDataBase = LiDarDataBase.getInstance(dirPathString+lidarDataPath);

        JsonArray lidarConfigs = liDarWorkers.getAsJsonArray("LidarConfigurations");
        List<LiDarWorkerTracker> lidarObjectList = new ArrayList<>();
        List<LiDarService> lidarServices = new ArrayList<>();

        if (lidarConfigs != null) {
            for (JsonElement element : lidarConfigs) {
                LiDarWorkerTracker lidarWorker = gson.fromJson(element, LiDarWorkerTracker.class);
                lidarWorker.setLidarDataBase(lidarDataBase);
                lidarWorker.postDeserialize();
                lidarObjectList.add(lidarWorker);
                lidarServices.add(new LiDarService(lidarWorker));
                numberofservices++;
            }
        }

        //*********************** Poses Setup ******************************//
        String poseDataPath = config.get("poseJsonFile").getAsString().substring(1);
        List<Pose> poseList = new ArrayList<>();

        try (FileReader poseReader = new FileReader(dirPathString+poseDataPath)) {
            JsonArray poseArray = gson.fromJson(poseReader, JsonArray.class);
            for (JsonElement element : poseArray) {
                Pose pose = gson.fromJson(element, Pose.class);
                poseList.add(pose);
            }
        } catch (FileNotFoundException e) {
            System.err.println("Pose data file not found: " + e.getMessage());
            return;
        } catch (IOException e) {
            System.err.println("Error reading pose data: " + e.getMessage());
            return;
        }

        GPSIMU gpsimu = new GPSIMU(0, poseList);
        PoseService poseService = new PoseService(gpsimu);

        //*********************** FusionSlam and Statistics ******************************//
        FusionSlam fusionSlam = FusionSlam.getInstance();
        ///NOAM - the +1 is for timeService? no need to count poseService and FusionSlamService?
        fusionSlam.setNumberOfNumberOfServices(lidarServices.size() + cameraServices.size() + 1);
        FusionSlamService fusionSlamService = new FusionSlamService(fusionSlam);

        //*********************** Simulation Setup ******************************//
        int tickTime = config.get("TickTime").getAsInt();
        int duration = config.get("Duration").getAsInt();
        TimeService timeService = new TimeService(tickTime, duration);
        ExecutorService executor = Executors.newFixedThreadPool(numberofservices);
        for (CameraService cameraService : cameraServices) {
            executor.submit(cameraService);
        }
        for (LiDarService lidarService : lidarServices) {
            executor.submit(lidarService);
        }
        executor.submit(poseService);
        executor.submit(fusionSlamService);
        executor.submit(timeService);


        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                System.err.println("Services did not terminate properly.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        //*********************** Parsing output file ******************************//
        StatisticalFolder stats = MicroService.getStatisticsInstance();
        System.out.println(stats.toString());
        OutputData outputData = OutputData.getInstance();

        outputData.setSystemRuntime(stats.getRunTime());
        outputData.setNumDetectedObjects(stats.getNumberOfDetectedObjects());
        outputData.setNumTrackedObjects(stats.getNumberOfTrackedObjects());
        outputData.setNumLandmarks(stats.getNumberOfLandmarks());
        outputData.setLandMarks((Map<String, LandMark>) fusionSlam.getLandMarks());

        // Create Gson with custom serializer
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(OutputData.class, new OutputSerializer())
                .create();

        String jsonOutput = gson.toJson(outputData);

        try (FileWriter writer = new FileWriter(dirPathString+"output_file.json")) {
            writer.write(jsonOutput);
            System.out.println("JSON output saved to: output_file.json");
        } catch (IOException e) {
            System.err.println("Error writing JSON file: " + e.getMessage());
        }

    }
}
