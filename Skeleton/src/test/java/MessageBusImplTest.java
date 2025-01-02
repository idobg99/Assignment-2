import bgu.spl.mics.*;
import bgu.spl.mics.application.messages.testEvent;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MessageBusImplTest {

    private MessageBus messageBus;
    private MicroService mockService1;
    private MicroService mockService2;
    private Event<String> mockEvent;
    private Broadcast mockBroadcast;

    @BeforeEach
    void setUp() {
        messageBus = MessageBusImpl.getBusInstance(); // Replace with your implementation
        mockService1 = mock(MicroService.class);        
        mockService2 = mock(MicroService.class);
        mockEvent = mock(testEvent.class);
        mockBroadcast = mock(Broadcast.class);
    }

    @Test
    void testSubscribeEvent() {
        messageBus.register(mockService1);              
        messageBus.subscribeEvent((Class<? extends Event<String>>) mockEvent.getClass(), mockService1);;
        Future<String> future = messageBus.sendEvent(mockEvent);
        assertNotNull(future, "Future should not be null for a subscribed event");
    }

    @Test
    void testSubscribeBroadcast() {
        messageBus.register(mockService1);
        messageBus.subscribeBroadcast(mockBroadcast.getClass(), mockService1);
        messageBus.sendBroadcast(mockBroadcast);
        Message message = assertDoesNotThrow(() -> messageBus.awaitMessage(mockService1));
        assertEquals(mockBroadcast, message, "Broadcast should be received by the microservice");
    }

    @Test
    void testCompleteEvent() {
        messageBus.register(mockService1);
        messageBus.subscribeEvent((Class<? extends Event<String>>) mockEvent.getClass(), mockService1);;

        Future<String> future = messageBus.sendEvent(mockEvent);
        assertNotNull(future, "Future should not be null for a subscribed event");

        String result = "Completed";
        messageBus.complete(mockEvent, result);
        assertTrue(future.isDone(), "Future should be marked as done after completion");
        assertEquals(result, future.get(), "Future should return the correct result");
    }

    @Test
    void testRegisterAndUnregister() {
        messageBus.register(mockService1);
        assertDoesNotThrow(() -> messageBus.awaitMessage(mockService1), "Registered microservice should not throw");

        messageBus.unregister(mockService1);
        assertThrows(IllegalStateException.class, () -> messageBus.awaitMessage(mockService1), "Unregistered microservice should throw");
    }

    @Test
    void testSendEventRoundRobin() {
        messageBus.register(mockService1);
        messageBus.register(mockService2);

        messageBus.subscribeEvent((Class<? extends Event<String>>) mockEvent.getClass(), mockService1);
        messageBus.subscribeEvent((Class<? extends Event<String>>) mockEvent.getClass(), mockService2);

        Future<String> future1 = messageBus.sendEvent(mockEvent);
        Future<String> future2 = messageBus.sendEvent(mockEvent);

        assertNotNull(future1, "First event should be handled by a microservice");
        assertNotNull(future2, "Second event should be handled by a microservice");
        assertNotEquals(future1, future2, "Events should be handled by different microservices");
    }
}
