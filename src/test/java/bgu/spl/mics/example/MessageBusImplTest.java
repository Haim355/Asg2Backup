package bgu.spl.mics.example;
import bgu.spl.mics.*;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.bind.SchemaOutputResolver;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import static org.junit.jupiter.api.Assertions.*;

class MessageBusImplTest {
    private Camera c1;
    private LiDarWorkerTracker lidar1;
    private LiDarWorkerTracker lidar2;
    private FusionSlam fusionSlamSingleton;
    private MessageBusImpl messageBus;
    private MicroService cameraService;
    private MicroService lidarService1;
    private MicroService lidarService2;
    private ERROR errorSingleton;
    private StatisticalFolder statistics;
    private FusionSlamService fusion;
    private TimeService timeSer;
    @BeforeEach
      void setUp() {
        c1 = new Camera(1, 3);
        lidar1 = new LiDarWorkerTracker(2, 3);
        lidar2 = new LiDarWorkerTracker(3, 3);
        fusionSlamSingleton = FusionSlam.getInstance();
        messageBus = MessageBusImpl.getInstance();
        cameraService = new CameraService(c1);
        lidarService1 = new LiDarService(lidar1);
        lidarService2 = new LiDarService(lidar2);
        fusion = new FusionSlamService(fusionSlamSingleton);
        timeSer = new TimeService(1, 15);
        messageBus.register(timeSer);
        messageBus.register(fusion);
        messageBus.register(cameraService);
        messageBus.register(lidarService1);
        messageBus.register(lidarService2);
        errorSingleton = ERROR.getInstance();
        statistics = StatisticalFolder.getInstance();
    }

    @AfterEach
    void tearDown(){
        messageBus.clearInstance();
        errorSingleton.clearInstance();
        statistics.clearInstance();
        fusionSlamSingleton.clearInstance();
    }
    /**
     * Precondition: The message bus is initialized, and no event is passed to it.
     * Postcondition: Sending a null event should not throw an exception or result in any message handling.
     */
    @Test
    void testNullEventHandling() {
        Future<Boolean> future = messageBus.sendEvent(null);
        assertNull(future, "Future is null for a null event is sent.");
    }
    /**
     * Precondition: The message bus is initialized, and no broadcast is passed to it.
     * Postcondition: Sending a null broadcast should not throw an exception or result in any message handling.
     */
    @Test
    void testNullBroadcastHandling() {
        assertDoesNotThrow(() -> messageBus.sendBroadcast(null), "Sending a null broadcast should not throw an exception.");
    }
    /**
     * Precondition: No microservice is subscribed to DetectObjectsEvent.
     * Postcondition: Sending an event with no subscribers should return a null future.
     */
    @Test
    void testEventWithNoSubscribers() {
        DetectObjectsEvent event = new DetectObjectsEvent(cameraService.getName(), new ArrayList<>(), 1);
        Future<Boolean> future = messageBus.sendEvent(event);
        assertNull(future, "Future should be null for an event with no subscribers.");
    }

    @Test
    void testMicroServicesRegistration() {

        List<MicroService> expectedMicroServices = new ArrayList<>();
        expectedMicroServices.add(timeSer);
        expectedMicroServices.add(fusion);
        expectedMicroServices.add(cameraService);
        expectedMicroServices.add(lidarService1);
        expectedMicroServices.add(lidarService2);
        ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>> subscribers = (ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>>) messageBus.getSubscribers();
        for (MicroService microService : expectedMicroServices) {
            boolean isRegistered = subscribers.containsKey(microService);

            assertTrue(isRegistered, "MicroService " + microService.getName() + " is registered to the MessageBus.");
            assertNotNull(subscribers.get(microService),  microService.getName() + " have been initialized message queue.");
        }

         assertEquals(expectedMicroServices.size(), subscribers.size(), "The total number of registered MicroServices should match the expected count.");
    }

    @Test
    void testDetectObjectsEventHandling() {
       messageBus.subscribeEvent(DetectObjectsEvent.class, lidarService1);
        messageBus.subscribeEvent(DetectObjectsEvent.class, lidarService2);

         List<DetectedObject> detectedObjects = new ArrayList<>();
        detectedObjects.add(new DetectedObject("1", "Tree"));
        detectedObjects.add(new DetectedObject("2", "Car"));

        DetectObjectsEvent event1 = new DetectObjectsEvent(cameraService.getName(), detectedObjects, 1);
        Future<Boolean> future1 = messageBus.sendEvent(event1);

        try {
            Message received1 = messageBus.awaitMessage(lidarService1);
            assertInstanceOf(DetectObjectsEvent.class, received1, "First message should be DetectObjectsEvent.");
            DetectObjectsEvent receivedEvent1 = (DetectObjectsEvent) received1;

            assertEquals(1, receivedEvent1.getTime(), "First event time should match.");
            assertEquals(2, receivedEvent1.getDetectedObject().size(), "First event's detected object list size should match.");
            assertEquals(event1.getSenderId(), receivedEvent1.getSenderId(), "First event's sender should match.");

            messageBus.complete(receivedEvent1, true);
            assertTrue(future1.isDone(), "Future1 should be resolved after completion.");
            assertTrue(future1.get(), "Future1 result should match the completion value.");

            //Creating a second event in order to test round-robin logic
            DetectObjectsEvent event2 = new DetectObjectsEvent(cameraService.getName(), detectedObjects, 2);
            Future<Boolean> future2 = messageBus.sendEvent(event2);

            // Second event should go to lidarService2
            Message received2 = messageBus.awaitMessage(lidarService2);
            assertInstanceOf(DetectObjectsEvent.class, received2, "Second message should be DetectObjectsEvent.");
            DetectObjectsEvent receivedEvent2 = (DetectObjectsEvent) received2;

            assertEquals(2, receivedEvent2.getTime(), "Second event time should match.");
            assertEquals(2, receivedEvent2.getDetectedObject().size(), "Second event's detected object list size should match.");
            assertEquals(event2.getSenderId(), receivedEvent2.getSenderId(), "Second event's sender should match.");

            // Complete the second event
            messageBus.complete(receivedEvent2, true);
            assertTrue(future2.isDone(), "Future2 should be resolved after completion.");
            assertTrue(future2.get(), "Future2 result should match the completion value.");
        } catch (InterruptedException e) {
            fail("Test was interrupted unexpectedly: " + e.getMessage());
        }
    }
    // Precondition: All services are registered, and no messages are processed yet.
    // Postcondition: All services terminate, and statistics are updated correctly.

    @Test
    void testAllEventTypesAndShutdown() {
       // Arrange: Setup events and expected statistics
        messageBus.subscribeEvent(DetectObjectsEvent.class, lidarService1);
        messageBus.subscribeEvent(TrackedObjectsEvent.class, fusion);
        messageBus.subscribeEvent(PoseEvent.class, fusion);
        List<DetectedObject> detectedObjects = new ArrayList<>();
        detectedObjects.add(new DetectedObject("100", "Tree"));
        detectedObjects.add(new DetectedObject("200", "Car"));

        ArrayList<CloudPoint> treeCoordinates = new ArrayList<>();
        treeCoordinates.add(new CloudPoint(1, 2.0));
        treeCoordinates.add(new CloudPoint(1.5, 2.5));

        ArrayList<CloudPoint> carCoordinates = new ArrayList<>();
        carCoordinates.add(new CloudPoint(3.0, 4.0));
        carCoordinates.add(new CloudPoint(3.5, 4.5));

        List<TrackedObject> trackedObjects = new ArrayList<>();
        trackedObjects.add(new TrackedObject("100", 1, "Tree", treeCoordinates));
        trackedObjects.add(new TrackedObject("200", 2, "Car", carCoordinates));

        Pose pose = new Pose(5.0f, 5.0f, 45.0f, 10);

        DetectObjectsEvent detectEvent = new DetectObjectsEvent(cameraService.getName(), detectedObjects, 1);
        TrackedObjectsEvent trackedEvent = new TrackedObjectsEvent(lidarService1.getName(), trackedObjects);
        PoseEvent poseEvent = new PoseEvent(fusion.getName(), pose, 3);

        Future<Boolean> detectFuture = messageBus.sendEvent(detectEvent);
        Future<Boolean> trackedFuture = messageBus.sendEvent(trackedEvent);
        Future<Boolean> poseFuture = messageBus.sendEvent(poseEvent);

        // Send ticks to simulate time progression
        for (int tick = 1; tick <= timeSer.getDuration(); tick++) {
            messageBus.sendBroadcast(new TickBroadcast(tick));
        }
         // Terminate all services after processing
        messageBus.sendBroadcast(new TerminatedBroadcast(timeSer));

        // Wait briefly for all services to finish
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            fail("Test interrupted unexpectedly.");
        }
       // Assert: Validate event handling and statistics
        try {
            // DetectObjectsEvent
            System.out.println("entered the try");
            Message detectMsg = messageBus.awaitMessage(lidarService1);
            assertInstanceOf(DetectObjectsEvent.class, detectMsg, "LiDarService1 should receive DetectObjectsEvent.");
            DetectObjectsEvent receivedDetectEvent = (DetectObjectsEvent) detectMsg;
            assertEquals(1, receivedDetectEvent.getTime(), "DetectObjectsEvent time should match.");
            assertEquals(2, receivedDetectEvent.getDetectedObject().size(), "DetectObjectsEvent detected objects size should match.");
            assertEquals("Tree", receivedDetectEvent.getDetectedObject().get(0).getDescription(), "First DetectedObject description should be Tree.");
            assertEquals("Car", receivedDetectEvent.getDetectedObject().get(1).getDescription(), "Second DetectedObject description should be Car.");
            messageBus.complete(receivedDetectEvent, true);
            assertTrue(detectFuture.isDone(), "DetectObjectsEvent future should be completed.");
            assertTrue(detectFuture.get(), "DetectObjectsEvent future result should be true.");

            // TrackedObjectsEvent
            Message trackedMsg = messageBus.awaitMessage(fusion);
            assertInstanceOf(TrackedObjectsEvent.class, trackedMsg, "FusionService should receive TrackedObjectsEvent.");
            TrackedObjectsEvent receivedTrackedEvent = (TrackedObjectsEvent) trackedMsg;
            assertEquals(2, receivedTrackedEvent.getTrackedObjectList().size(), "TrackedObjectsEvent list size should match.");
            assertEquals("Tree", receivedTrackedEvent.getTrackedObjectList().get(0).getDescription(), "First TrackedObject description should be Tree.");
            assertEquals("Car", receivedTrackedEvent.getTrackedObjectList().get(1).getDescription(), "Second TrackedObject description should be Car.");
            assertEquals(2, receivedTrackedEvent.getTrackedObjectList().get(1).getCoordinates().size(), "Coordinates size for Car should match.");
            messageBus.complete(receivedTrackedEvent, true);
            assertTrue(trackedFuture.isDone(), "TrackedObjectsEvent future should be completed.");
            assertTrue(trackedFuture.get(), "TrackedObjectsEvent future result should be true.");

            // PoseEvent
            Message poseMsg = messageBus.awaitMessage(fusion);
            assertInstanceOf(PoseEvent.class, poseMsg, "FusionService should receive PoseEvent.");
            PoseEvent receivedPoseEvent = (PoseEvent) poseMsg;
            assertEquals(10, receivedPoseEvent.getPose().getTime(), "PoseEvent time should match.");
            messageBus.complete(receivedPoseEvent, true);
            assertTrue(poseFuture.isDone(), "PoseEvent future should be completed.");
            assertTrue(poseFuture.get(), "PoseEvent future result should be true.");
        } catch (InterruptedException e) {
            fail("Unexpected exception: " + e.getMessage());
        }

        assertEquals(15, statistics.getRunTime(), "RunTime should be 15.");
        assertEquals(2, statistics.getNumberOfDetectedObjects().get(), "Number of detected objects should be 2.");
        assertEquals(2, statistics.getNumberOfTrackedObjects().get(), "Number of tracked objects should be 2.");

   }

}

    /*@Test
    void testErrorHandlingWithCrashedBroadcast() {

        int crashTime = 5; int totalTicks = 10;
        int cameraFrequency = c1.getFrequency();
        int timeEvent1 = 1; int timeEvent2 = 5;
        List<DetectedObject> detectedObjects = new ArrayList<>();
        detectedObjects.add(new DetectedObject("1", "Tree"));
        detectedObjects.add(new DetectedObject("2", "Car"));
        String expectedErrorMessage = "";
        while (timeSer.getCurrentTick() < 10){
              timeSer.incrementCurrentTick();
              messageBus.sendBroadcast(new TickBroadcast(timeSer.getCurrentTick()));
              if (timeSer.getCurrentTick() == timeEvent1) {
                DetectObjectsEvent event1 = new DetectObjectsEvent(cameraService.getName(), detectedObjects, timeSer.getCurrentTick());
                messageBus.sendEvent(event1);
              }
              if (timeSer.getCurrentTick() == timeEvent2){
                  DetectObjectsEvent event2 = new DetectObjectsEvent(cameraService.getName(), detectedObjects, timeSer.getCurrentTick());
                  messageBus.sendEvent(event2);
              }
              if (timeSer.getCurrentTick() == crashTime) {
                  lidar1.setErrorMessage();
                  expectedErrorMessage = lidar1.getErrorMessage();
                  messageBus.sendBroadcast(new CrahsedBroadCast());
                  break;
              }
        }
        try {
           Message receivedEvent = messageBus.awaitMessage(lidarService1);
            assertInstanceOf(DetectObjectsEvent.class, receivedEvent, "receivedEvent correct");
            DetectObjectsEvent detectEvent = (DetectObjectsEvent) receivedEvent;
            assertEquals(detectedObjects, detectEvent.getDetectedObject(), "detectedObjects correct");
            assertEquals(cameraService.getName(), detectEvent.getSenderId(), "senderId correct");
            assertEquals(timeEvent1 + cameraFrequency, detectEvent.getTime(), "recivedEvent1 time is correct");
            Message crashReceived = messageBus.awaitMessage(lidarService1);
            assertInstanceOf(CrahsedBroadCast.class, crashReceived, "crashBroadcast correct");

            Message crashReceived2 = messageBus.awaitMessage(lidarService2);
            assertInstanceOf(CrahsedBroadCast.class, crashReceived2, "crashBroadcastReception correct");

            ERROR programErrorInstance = ERROR.getInstance();
            assertNotNull(programErrorInstance.getFaultySensor(), "faultySensor correct");
            assertEquals(lidarService1.getName(), programErrorInstance.getFaultySensor(), "faultySensor correct");
            assertNotNull(expectedErrorMessage, "expectedErrorMessage is not null");
            assertEquals(expectedErrorMessage, programErrorInstance.getErrorDescription(), "errorDescription correct");
            assertEquals(crashTime, programErrorInstance.getTime(), "time correct");

            StatisticalFolder programStatistics = StatisticalFolder.getInstance();
            assertEquals(totalTicks, programStatistics.getRunTime(), "runTime correct");
            assertEquals(detectedObjects.size(), programStatistics.getNumberOfDetectedObjects().get(),
                    "detectedObjectsCount correct");
        } catch (InterruptedException e) {
            fail("Test was interrupted unexpectedly: " + e.getMessage());
        }
    }
*/

