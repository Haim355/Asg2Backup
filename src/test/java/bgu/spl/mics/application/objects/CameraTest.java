package bgu.spl.mics.application.objects;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class CameraTest {

    private Camera c1;

    @BeforeEach
    void setUp() {
        c1 = new Camera(1, 5);
        c1.setStatus(STATUS.UP);
    }

    @Test
    public void testGetStampedByTime_ValidTimeAndObjectExists() {
        DetectedObject obj1 = new DetectedObject("1", "wall1");
        DetectedObject obj2 = new DetectedObject("2", "wall2");
        List<DetectedObject> lst1 = new ArrayList<>();
        lst1.add(obj1);
        List<DetectedObject> lst2 = new ArrayList<>();
        lst2.add(obj2);
        StampedDetectedObjects stObj1 = new StampedDetectedObjects(10, lst1);
        StampedDetectedObjects stObj2 = new StampedDetectedObjects(20, lst2);
        List<StampedDetectedObjects> detectedObjects = new ArrayList<>();
        detectedObjects.add(stObj1);
        detectedObjects.add(stObj2);

        c1.setStampedDetectedObjects(detectedObjects);

        StampedDetectedObjects result = c1.getStampedByTime(10);

        assertNotNull(result);
        assertEquals(10, result.getTime());
        assertEquals(STATUS.UP, c1.getStatus());
        assertEquals(1, c1.getCounter());
        assertEquals(lst1, c1.getDetectedObjects().get(0).getDetectedObjects());
        assertEquals(stObj2, c1.getNext());
    }
    void testGetStampedByTime_ValidTimeLastObject() {
        DetectedObject obj1 = new DetectedObject("1", "wall1");
        DetectedObject obj2 = new DetectedObject("2", "wall2");
        List<DetectedObject> lst1 = new ArrayList<>();
        lst1.add(obj1);
        List<DetectedObject> lst2 = new ArrayList<>();
        lst2.add(obj2);
        StampedDetectedObjects stObj1 = new StampedDetectedObjects(10, lst1);
        StampedDetectedObjects stObj2 = new StampedDetectedObjects(20, lst2);
        List<StampedDetectedObjects> detectedObjects = new ArrayList<>();
        detectedObjects.add(stObj1);
        detectedObjects.add(stObj2);
        c1.setStampedDetectedObjects(detectedObjects);
        c1.setCounter(1); // Start at second object
        c1.setNext(stObj2);

        StampedDetectedObjects result = c1.getStampedByTime(20);

        assertNotNull(result);
        assertEquals(20, result.getTime());
        assertEquals(STATUS.DOWN, c1.getStatus());
        assertEquals(2, c1.getCounter());
        assertNull(c1.getNext());
    }
    @Test
    public void testGetStampedByTime_ErrorAtfirstObject() {

        DetectedObject obj1 = new DetectedObject("1","wall1" );
        DetectedObject obj2 = new DetectedObject("ERROR", "CAMERA DISCONNECTED");
        DetectedObject obj3 = new DetectedObject("3", "wall3");
        List<DetectedObject> lst1 = new ArrayList<>();
        lst1.add(obj1);lst1.add(obj2);
        List<DetectedObject> lst2 = new ArrayList<>();
        lst2.add(obj3);
        StampedDetectedObjects stObj1 = new StampedDetectedObjects(10, lst1);
        StampedDetectedObjects stObj2 = new StampedDetectedObjects(20, lst2);
        List<StampedDetectedObjects> detectedObjects = new ArrayList<>();
        detectedObjects.add(stObj1);
        detectedObjects.add(stObj2);
        c1.setStampedDetectedObjects(detectedObjects);

        StampedDetectedObjects result = c1.getStampedByTime(10);

        assertNotNull(result);
        assertEquals(10, result.getTime());
        assertEquals(STATUS.ERROR, c1.getStatus());
        assertEquals("CAMERA DISCONNECTED", c1.getErrorMessage());
        assertEquals(0, c1.getCounter()); // Counter should not increment on error
        assertEquals(stObj1, c1.getNext()); // "next" should not advance on error
    }



}
