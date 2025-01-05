 
import bgu.spl.mics.*;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.testEvent;
import bgu.spl.mics.application.services.*; 
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class massageBusTest {
    
        private MessageBus messageBus;
        private MicroService Service1;
        private MicroService Service2;
        private Event<String> Event;
        private Broadcast Broadcast;
    
        @BeforeEach
        void setUp() {
            messageBus = MessageBusImpl.getBusInstance(); 
            Service1 = new testService();                  
            Service2 = new testService();
            Event = new testEvent("hello");
            Broadcast = new TickBroadcast(1);
        }
    
        @Test
        void testSubscribeEvent() {
            messageBus.register(Service1);                         
            messageBus.subscribeEvent(testEvent.class,Service1);            
            Future<String> future = messageBus.sendEvent(Event);
            messageBus.complete(Event,"hello");
            assertNotNull(future, "Future should not be null for a subscribed event");
            assertEquals("hello",future.get(), "Future shoule be hello");
        }
    
        @Test
        void testSubscribeBroadcast() {
            messageBus.register(Service2);
            messageBus.subscribeBroadcast(Broadcast.getClass(), Service2);
            messageBus.sendBroadcast(Broadcast);
            Message message = assertDoesNotThrow(() -> messageBus.awaitMessage(Service2));
            assertEquals(Broadcast, message, "Broadcast should be received by the microservice");
        }
    
        @Test
        void testCompleteEvent() {
            messageBus.register(Service1);
            messageBus.subscribeEvent(testEvent.class, Service1);;    
            Future<String> future = messageBus.sendEvent(Event);
            assertNotNull(future, "Future should not be null for a subscribed event");   
            String result = "Completed";
            messageBus.complete(Event, result);
            assertTrue(future.isDone(), "Future should be marked as done after completion");
            assertEquals(result, future.get(), "Future should return the correct result");
        }
}
    

