package bgu.spl.mics.application.messages;

import bgu.spl.mics.EventImp;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.TrackedObject;

import java.awt.*;
import java.util.List;

public class TrackedObjectsEvent extends EventImp<Boolean> /*now check if the future should be ?*/ {
    private List<TrackedObject> trackedObjectList;

    public TrackedObjectsEvent(String SenderId, List<TrackedObject> trackedObjects) {
        super(SenderId);
        this.trackedObjectList = trackedObjects;
    }

    public List<TrackedObject> getTrackedObjectList() {
        return trackedObjectList;
    }
}
