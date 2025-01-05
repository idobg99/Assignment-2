import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import bgu.spl.mics.application.objects.*;
import static org.junit.jupiter.api.Assertions.*;

class LidarTest {

    private LiDarWorkerTracker tracker;
    private LiDarDataBase LidarDB;
    //private StatisticalFolder StatlFolder;

    @BeforeEach
    void setUp() {
        //MockitoAnnotations.openMocks(this);
        tracker = new LiDarWorkerTracker(1, 2);       
        LidarDB = LiDarDataBase.getInstance();
        //StatlFolder = StatisticalFolder.getInstance();
    }

    @Test
    void testTrackObject_ValidId() {
        // Arrange
        String validId = "object123";
        String description = "A valid object";
        int time = 5;

        CloudPoint point1 = new CloudPoint(1.0, 2.0);
        LidarDB.insertSingular(new StampedCloudPoints(5, validId, Collections.singletonList(point1)));
        
        // Act
        TrackedObject result = tracker.trackObject(time, validId, description);

        // Assert
        assertNotNull(result, "TrackedObject should not be null for valid ID.");
        assertEquals(validId, result.getId(), "TrackedObject ID should match.");
        assertEquals(description, result.getDescription(), "TrackedObject description should match.");
    }

    @Test
    void testTrackObject_IdNotFound() {
        // Arrange
        String invalidId = "invalidId";
        String description = "Description";
        int time = 5;

        // Act
        TrackedObject result = tracker.trackObject(time, invalidId, description);

        // Assert
        assertNull(result, "TrackedObject should be null for ID not found.");
    }
}
