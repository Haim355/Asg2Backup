package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.TimeService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CameraConfig {
        private ArrayList<Camera> cameras;
        public ArrayList<Camera> getCameras(){return this.cameras;}
        public void setCameras(ArrayList<Camera> cameras){this.cameras = cameras;}
}
