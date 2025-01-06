package bgu.spl.mics.application.objects;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Configuration {
        private ArrayList<Camera> Cameras;
        private String cameraJsonFile;
        private ArrayList<LiDarWorkerTracker> LiDarWorkers;
        private String lidarJsonFile;
        private String poseJsonFile;
        private int TickTime;
        private int Duration;


        public ArrayList<Camera> getCameras() { return Cameras; }
        public String getCameraJsonFile() { return cameraJsonFile; }
        public ArrayList<LiDarWorkerTracker> getLiDarWorkers() { return LiDarWorkers; }
        public String getLidarJsonFile() { return lidarJsonFile; }
        public String getPoseJsonFile() { return poseJsonFile; }
        public int getTickTime() { return TickTime; }
        public int getDuration() { return Duration; }
}




