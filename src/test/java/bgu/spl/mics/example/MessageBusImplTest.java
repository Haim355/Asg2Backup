package bgu.spl.mics.example;
import bgu.spl.mics.*;
import bgu.spl.mics.Future;
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
import java.util.concurrent.*;

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
    private ExecutorService executor;

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
        executor = Executors.newFixedThreadPool(5);
    }

    @AfterEach
    void tearDown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

        messageBus.clearInstance();
        fusionSlamSingleton.clearInstance();
    }

    /**
     * Precondition: The message bus is initialized, and no event is passed to it.
     * Postcondition: Sending a null event should not throw an exception or result in any message handling.
     */
    @Test
    void testNullSendEventHandling() {
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
    void testSendEventWithNoSubscribers() {
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
            assertNotNull(subscribers.get(microService), microService.getName() + " have been initialized message queue.");
        }

        assertEquals(expectedMicroServices.size(), subscribers.size(), "The total number of registered MicroServices should match the expected count.");
    }

    @Test
    void testRoundRobinLogicForEvents() {
        messageBus.subscribeEvent(DetectObjectsEvent.class, lidarService1);
        messageBus.subscribeEvent(DetectObjectsEvent.class, lidarService2);

        List<DetectedObject> detectedObjects = new ArrayList<>();
        detectedObjects.add(new DetectedObject("1", "Tree"));
        detectedObjects.add(new DetectedObject("2", "Car"));

        DetectObjectsEvent event1 = new DetectObjectsEvent(cameraService.getName(), detectedObjects, 1);
        int roundRobinIndex1 = messageBus.getRoundRobinIndexByService(event1.getClass());
        Future<Boolean> future1 = messageBus.sendEvent(event1);

        DetectObjectsEvent event2 = new DetectObjectsEvent(cameraService.getName(), detectedObjects, 2);
        int roundRobinIndex2 = messageBus.getRoundRobinIndexByService(event2.getClass());
        Future<Boolean> future2 = messageBus.sendEvent(event2);

        try {
            Message received1 = messageBus.awaitMessage(lidarService1);
            assertInstanceOf(DetectObjectsEvent.class, received1, "First message should be DetectObjectsEvent.");
            DetectObjectsEvent receivedEvent1 = (DetectObjectsEvent) received1;

            assertEquals(0, roundRobinIndex1, "Round-robin index for the first event should be 0.");
            assertEquals(event1.getSenderId(), receivedEvent1.getSenderId(), "Event 1 sender should match.");
            messageBus.complete(receivedEvent1, true);
            assertTrue(future1.isDone(), "Future1 should be resolved after completion.");
            assertTrue(future1.get(), "Future1 result should be true.");

            Message received2 = messageBus.awaitMessage(lidarService2);
            assertInstanceOf(DetectObjectsEvent.class, received2, "Second message should be DetectObjectsEvent.");
            DetectObjectsEvent receivedEvent2 = (DetectObjectsEvent) received2;

            assertEquals(1, roundRobinIndex2, "Round-robin index for the second event should be 1.");
            assertEquals(event2.getSenderId(), receivedEvent2.getSenderId(), "Event 2 sender should match.");
            messageBus.complete(receivedEvent2, true);
            assertTrue(future2.isDone(), "Future2 should be resolved after completion.");
            assertTrue(future2.get(), "Future2 result should be true.");
        } catch (InterruptedException e) {
            fail("Test was interrupted unexpectedly: " + e.getMessage());
        }
    }

}
