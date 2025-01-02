
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.LandMark;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.StampedCloudPoints;
import bgu.spl.mics.application.objects.TrackedObject;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FusionSlamTest {

    private FusionSlam fusionSlam;

    @BeforeEach
    void setUp() {
        fusionSlam = FusionSlam.getInstance();
    }

    @Test
    void testCalculateLandMark_WithValidInput() {
        // Arrange
        CloudPoint posePoint = new CloudPoint(1.0, 2.0);
        Pose currentPose = new Pose(10, posePoint, 45.0f);

        CloudPoint objPoint1 = new CloudPoint(1.0, 0.0);
        CloudPoint objPoint2 = new CloudPoint(0.0, 1.0);

        StampedCloudPoints stampedPoints = new StampedCloudPoints(10, "object1", Arrays.asList(objPoint1, objPoint2));
        TrackedObject trackedObject = new TrackedObject(stampedPoints, "Test Object");

        LandMark result = fusionSlam.calculteLandMark(trackedObject, currentPose);

        // Assert
        assertNotNull(result, "Resulting landmark should not be null");
        assertEquals("object1", result.getId(), "Landmark ID should match tracked object's ID");
        assertEquals("Test Object", result.getDescription(), "Landmark description should match tracked object's description");

        List<CloudPoint> expectedCoordinates = Arrays.asList(
                new CloudPoint(1.0, 2.0 + Math.sqrt(2)),
                new CloudPoint(1.0 + Math.sqrt(2), 2.0)
        );

        List<CloudPoint> actualCoordinates = result.getCoordinates();
        assertEquals(expectedCoordinates.size(), actualCoordinates.size(), "Number of coordinates should match");

        for (int i = 0; i < expectedCoordinates.size(); i++) {
            assertEquals(expectedCoordinates.get(i).getX(), actualCoordinates.get(i).getX(), 0.001, "X coordinate mismatch");
            assertEquals(expectedCoordinates.get(i).getY(), actualCoordinates.get(i).getY(), 0.001, "Y coordinate mismatch");
        }
    }

    @Test
    void testCalculateLandMark_WithNullPose() {
        // Arrange
        CloudPoint objPoint1 = new CloudPoint(1.0, 0.0);
        StampedCloudPoints stampedPoints = new StampedCloudPoints(10, "object1", Arrays.asList(objPoint1));
        TrackedObject trackedObject = new TrackedObject(stampedPoints, "Test Object");

        // Act
        LandMark result = fusionSlam.calculteLandMark(trackedObject, null);

        // Assert
        assertNull(result, "Result should be null when current pose is null");
    }   
}
