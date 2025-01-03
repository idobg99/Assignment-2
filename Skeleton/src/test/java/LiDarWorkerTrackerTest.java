import bgu.spl.mics.application.messages.DetectObjectsEvent;
    import bgu.spl.mics.application.messages.TrackedObjectsEvent;
    import org.junit.jupiter.api.BeforeEach;
    import org.junit.jupiter.api.Test;
    
    import java.util.Arrays;
    import java.util.Collections;
    import bgu.spl.mics.application.objects.CloudPoint;
    import bgu.spl.mics.application.objects.FusionSlam;
    import bgu.spl.mics.application.objects.LandMark;
    import bgu.spl.mics.application.objects.Pose;
    import bgu.spl.mics.application.objects.StampedCloudPoints;
    import bgu.spl.mics.application.objects.TrackedObject;
    import bgu.spl.mics.application.objects.LiDarWorkerTracker;
    import bgu.spl.mics.application.objects.LiDarDataBase;
    import bgu.spl.mics.application.objects.DetectedObject;
    import bgu.spl.mics.application.objects.StampedDetectedObjects;
    import static org.junit.jupiter.api.Assertions.*;
    import static org.mockito.Mockito.*;
    
    class LiDarWorkerTrackerTest {
    
        private LiDarWorkerTracker tracker;
        private LiDarDataBase mockLidarDB;
        
        @BeforeEach
        void setUp() {
            mockLidarDB = mock(LiDarDataBase.class);
            tracker = new LiDarWorkerTracker(1, 10); 
        }
    
        @Test
        void testDetectTotrackObject_ValidInput() {
            // Arrange
            CloudPoint point1 = new CloudPoint(1.0, 2.0);
            StampedCloudPoints stampedPoints = new StampedCloudPoints(10, "obj1", Collections.singletonList(point1));
            DetectedObject detectedObject = new DetectedObject("obj1", "Object 1");
            StampedDetectedObjects detectedObjects = new StampedDetectedObjects(10, Collections.singletonList(detectedObject));
    
            when(mockLidarDB.getStampedCloudPoints(10)).thenReturn(Collections.singletonList(stampedPoints));
    
            DetectObjectsEvent event = new DetectObjectsEvent(detectedObjects);
    
            // Act
            TrackedObjectsEvent result = tracker.DetectTotrackObject(event);
    
            // Assert
            assertNotNull(result, "Resulting TrackedObjectsEvent should not be null");
            assertEquals(1, result.getTrackedObjects().size(), "Tracked objects list size should match");
            TrackedObject trackedObject = result.getTrackedObjects().get(0);
            assertEquals("obj1", trackedObject.getId(), "Tracked object ID should match detected object ID");
            assertEquals("Object 1", trackedObject.getDescription(), "Tracked object description should match detected object description");
            verify(mockLidarDB, times(1)).getStampedCloudPoints(10);
        }
    
        @Test
        void testDetectTotrackObject_InvalidObject() {
            // Arrange
            StampedCloudPoints stampedPoints = new StampedCloudPoints(10, "Error", Collections.emptyList());
            DetectedObject detectedObject = new DetectedObject("obj1", "Object 1");
            StampedDetectedObjects detectedObjects = new StampedDetectedObjects(10, Collections.singletonList(detectedObject));
    
            when(mockLidarDB.getStampedCloudPoints(10)).thenReturn(Collections.singletonList(stampedPoints));
    
            DetectObjectsEvent event = new DetectObjectsEvent(detectedObjects);
    
            // Act
            TrackedObjectsEvent result = tracker.DetectTotrackObject(event);
    
            // Assert
            assertNotNull(result, "Resulting TrackedObjectsEvent should not be null");
            assertNull(result.getTrackedObjects(), "Tracked objects list should be null when error is found");
        }
    
        @Test
        void testDetectTotrackObject_NoMatch() {
            // Arrange
            StampedCloudPoints stampedPoints = new StampedCloudPoints(10, "obj2", Collections.emptyList());
            DetectedObject detectedObject = new DetectedObject("obj1", "Object 1");
            StampedDetectedObjects detectedObjects = new StampedDetectedObjects(10, Collections.singletonList(detectedObject));
    
            when(mockLidarDB.getStampedCloudPoints(10)).thenReturn(Collections.singletonList(stampedPoints));
    
            DetectObjectsEvent event = new DetectObjectsEvent(detectedObjects);
    
            // Act
            TrackedObjectsEvent result = tracker.DetectTotrackObject(event);
    
            // Assert
            assertNotNull(result, "Resulting TrackedObjectsEvent should not be null");
            assertEquals(0, result.getTrackedObjects().size(), "Tracked objects list size should be zero when no match found");
        }
    }
    
    
    
    
    
    
