package bgu.spl.mics.application.messages;

import bgu.spl.mics.EventImp;
import bgu.spl.mics.application.objects.Pose;

import java.awt.*;

public class PoseEvent extends EventImp<Boolean> {
    Pose currentpose;
    int time;
    public PoseEvent(String SenderId,Pose pose,int time) {
        super(SenderId);
        this.currentpose=pose;
        this.time=time;
    }
    public Pose getPose() {
        return currentpose;
    }
    public int getTime() {
        return time;
    }
}
