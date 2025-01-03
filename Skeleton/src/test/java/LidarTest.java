
import bgu.spl.mics.application.messages.DetectObjectsEvent;
    import bgu.spl.mics.application.messages.TrackedObjectsEvent;
    import org.junit.jupiter.api.BeforeEach;
    import org.junit.jupiter.api.Test;
    
    import java.util.Arrays;
    import java.util.Collections;
    import bgu.spl.mics.application.objects.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

class LidarTest {

    private LiDarWorkerTracker tracker;
    private LiDarDataBase mockLidarDB;
    private StatisticalFolder mockStatisticalFolder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tracker = new LiDarWorkerTracker(1, 10);       
        mockLidarDB = mock(LiDarDataBase.class);
        mockStatisticalFolder = mock(StatisticalFolder.class);
        List<TrackedObject> lastTrackedObjects = new ArrayList<>();
    }

    @Test
    void testTrackObject_ValidId() {
        // Arrange
        String validId = "object123";
        String description = "A valid object";
        int time = 5;

        CloudPoint point1 = new CloudPoint(1.0, 2.0);
        List<StampedCloudPoints> cloudPoints = new ArrayList<>();
        cloudPoints.add(new StampedCloudPoints(5, validId, Collections.singletonList(point1)));
        //StampedCloudPoints cloud = new StampedCloudPoints(5, validId, Collections.singletonList(point1));
        //when(mockLidarDB.getStampedCloudPoints(5).thenReturn(cloudPoints);
        when(mockLidarDB.getStampedCloudPoints(anyInt())).thenReturn(cloudPoints);
        
        // Act
        TrackedObject result = tracker.trackObject(time, validId, description);

        // Assert
        //assertNotNull(result, "TrackedObject should not be null for valid ID.");
        assertEquals(validId, result.getId(), "TrackedObject ID should match.");
        assertEquals(description, result.getDescription(), "TrackedObject description should match.");
        verify(mockStatisticalFolder, never()).logError(anyString());
    }
}
//     @Test
//     void testTrackObject_IdNotFound() {
//         // Arrange
//         String invalidId = "invalidId";
//         String description = "Description";
//         int time = 5;

//         List<StampedCloudPoints> emptyCloudPoints = Collections.emptyList();
//         when(mockLidarDB.getStampedCloudPoints(anyInt())).thenReturn(emptyCloudPoints);

//         // Act
//         TrackedObject result = tracker.trackObject(time, invalidId, description);

//         // Assert
//         assertNull(result, "TrackedObject should be null for ID not found.");
//         verify(mockStatisticalFolder).logError(contains("Error Not Found"));
//     }

// }