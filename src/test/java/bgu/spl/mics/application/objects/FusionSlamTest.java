package bgu.spl.mics.application.objects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.bind.SchemaOutputResolver;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import static org.junit.jupiter.api.Assertions.*;

    class FusionSlamTest {

        private FusionSlam fusionSlam;

        @BeforeEach
        void setUp() {
            fusionSlam = FusionSlam.getInstance();
        }
        @AfterEach
        void tearDown() {
            fusionSlam.clearInstance();
        }
        @Test
        void testAddNewLandMarkWithExistingPose() {
            Pose pose = new Pose(10, 20, 45, 100);
            fusionSlam.addPose(pose);

            List<CloudPoint> coordinates = new ArrayList<>();
            coordinates.add(new CloudPoint(1, 2));
            coordinates.add(new CloudPoint(3, 4));

            fusionSlam.addLandMark("1", "LM1", coordinates, 100);

            List<CloudPoint> globalCoords = fusionSlam.Convertor(coordinates,pose);
            ConcurrentHashMap<String, LandMark> landmarks = fusionSlam.getLandMarks();
            assertNotNull(globalCoords, "globalCoords are not null");
            assertNotNull(landmarks, "LandMarks is not null");
            assertTrue(landmarks.containsKey("1"), "Landmark 1 added.");
            Assertions.assertEquals(1,landmarks.size(), "Landmarks size incremented by 1");
            LandMark landMark = landmarks.get("1");
            assertEquals(landMark.getCoordinates(), globalCoords);
            Assertions.assertEquals("LM1", landMark.getDescription(), "Landmark 1 description match.");
            Assertions.assertEquals(2, landMark.getCoordinates().size(), "Landmark 1 coordinates match.");
        }

        private static void assertEquals (List<CloudPoint> lst1, List<CloudPoint> lst2 ){
            if (lst1.size() != lst2.size()){
                System.out.println("The lists are not equal");
            }
            else {
                Iterator it1 = lst1.iterator();
                Iterator it2 = lst2.iterator();
                while (it1.hasNext()){
                    if (! it2.next().equals(it1.next())){
                        System.out.println("The lists are not matching");
                    }
                }
                System.out.println("The lists are identical");
            }
        }

        @Test
        void testAddLandMark_UpdateExistingLandMark_NonEqualSizedCoordsLists() {

            Pose pose = new Pose(10, 20, 45, 100);
            fusionSlam.addPose(pose);

            List<CloudPoint> coord1 = new ArrayList<>();
            coord1.add(new CloudPoint(1, 2));
            coord1.add(new CloudPoint(2,2));
            List<CloudPoint> coord2 = new ArrayList<>();
            coord2.add(new CloudPoint(3, 4));

            ConcurrentHashMap<String, LandMark> landmarks = fusionSlam.getLandMarks();
            fusionSlam.addLandMark("1", "LM1", coord1, 100);
            List<CloudPoint> global1 = landmarks.get("1").getCoordinates();
            fusionSlam.addLandMark("1", "LM1", coord2, 101);
            List<CloudPoint> global2 = fusionSlam.Convertor(coord2,pose);
            List<CloudPoint> result = fusionSlam.averageCoordinates(global1, global2);
            LandMark landMark = landmarks.get("1");
            Assertions.assertEquals(1,landmarks.size(), "LandMarks size havent changed");
            assertTrue(landmarks.containsKey("1"), "Landmark 1 exist");
            Assertions.assertEquals(2, landMark.getCoordinates().size(), "Coordinates should be updated.");
            assertEquals(result, landMark.getCoordinates());

        }

        @Test
        void testAddLandMarkWithoutExistingPose() {
            //Case in which there is no data about the robots current pose
            List<CloudPoint> coordinates = new ArrayList<>();
            coordinates.add(new CloudPoint(1, 2));

            fusionSlam.addLandMark("1","PendingLandMark" , coordinates, 10);
            ConcurrentHashMap<String, LandMark> landmarks = fusionSlam.getLandMarks();
            List<LandMark> pending = fusionSlam.getPending();
            Assertions.assertEquals(0,landmarks.size(), "LandMarks is empty after doing the addLandMark method");
            Assertions.assertEquals(1, pending.size(), "Pending contains a new Landmark");
            Assertions.assertEquals("1", pending.get(0).getId(), "Pending landmark ID match.");
        }
    }


