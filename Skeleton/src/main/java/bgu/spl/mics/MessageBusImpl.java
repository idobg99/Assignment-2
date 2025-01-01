package bgu.spl.mics;

import java.util.concurrent.*;
import java.util.*;
import java.util.concurrent.locks.*;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only one public method (in addition to getters which can be public solely for unit testing) may be added to this class
 * All other methods and members you add the class must be private.
 */
public class MessageBusImpl implements MessageBus {
	private static class SingletonHolder {
        private static MessageBusImpl INSTANCE = new MessageBusImpl();
    }
	
	private ConcurrentHashMap<MicroService, BlockingQueue<Message>> microServiceQueues;
    private ConcurrentHashMap<Class<? extends Event<?>>, Queue<MicroService>> eventSubscribers;
    private ConcurrentHashMap<Class<? extends Broadcast>, CopyOnWriteArrayList<MicroService>> broadcastSubscribers;
    private ConcurrentHashMap<Event<?>, Future<?>> eventFutures;
    private final ReadWriteLock lock;

	private MessageBusImpl(){   //private constructor for the singelton class
		microServiceQueues = new ConcurrentHashMap<>();
		eventSubscribers = new ConcurrentHashMap<>();
		broadcastSubscribers = new ConcurrentHashMap<>();
		eventFutures = new ConcurrentHashMap<>();
		lock = new ReentrantReadWriteLock();	
	}  
	public static MessageBusImpl getBusInstance() {
        return SingletonHolder.INSTANCE;
    } 
	
	@Override
	public <T> void subscribeEvent( Class<? extends Event<T>> type, MicroService m) { // MAKE SURE THAT IS THREAD SAFE
		lock.writeLock().lock();
        try {
            eventSubscribers.putIfAbsent(type, new ConcurrentLinkedQueue<>());
            eventSubscribers.get(type).add(m);
        } finally {
            lock.writeLock().unlock();
        }
    }	

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) { // MAKE SURE THAT IS THREAD SAFE
		broadcastSubscribers.putIfAbsent(type, new CopyOnWriteArrayList<>());
        broadcastSubscribers.get(type).add(m);
    }

	@Override
	public <T> void complete(Event<T> e, T result) {
		@SuppressWarnings("unchecked")
        Future<T> future = (Future<T>) eventFutures.remove(e);
        if (future != null) {
            future.resolve(result);
        }
	}

	@Override
	public void sendBroadcast(Broadcast b) { // MAKE SURE THAT IS THREAD SAFE
		CopyOnWriteArrayList<MicroService> subscribers = broadcastSubscribers.get(b.getClass());
        if (subscribers != null) {
            for (MicroService m : subscribers) {

                System.out.println("SENDING BROADCAST TO SERVICE - " + m.getName());;

                BlockingQueue<Message> queue = microServiceQueues.get(m);
                if (queue != null) {
                    queue.offer(b);
                }
            }
        }
    }
	@Override
	public <T> Future<T> sendEvent(Event<T> e) { // MAKE SURE THAT IS THREAD SAFE
		Queue<MicroService> subscribers = eventSubscribers.get(e.getClass());
        if (subscribers == null || subscribers.isEmpty()) {
            return null;
        }
        MicroService m;
        synchronized (subscribers) {
            m = subscribers.poll();
            subscribers.offer(m); // Round-robin
        }

        if (m != null) {
            Future<T> future = new Future<T>();
            eventFutures.put(e, future);
            microServiceQueues.get(m).offer(e);
            return future;
        }
        return null;
    }

	@Override
	public void register(MicroService m) { // MAKE SURE THAT IS THREAD SAFE
		microServiceQueues.putIfAbsent(m, new LinkedBlockingQueue<>());
    }

	@Override
	public void unregister(MicroService m) { // MAKE SURE THAT IS THREAD SAFE
        System.out.println("Unregistering MicroService: " + m.getName());
		lock.writeLock().lock();
        try {
            microServiceQueues.remove(m);
            eventSubscribers.values().forEach(queue -> queue.remove(m));
            broadcastSubscribers.values().forEach(list -> list.remove(m));
        } finally {
            lock.writeLock().unlock();
        }
    }
	
	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException { // MAKE SURE THAT IS THREAD SAFE
        BlockingQueue<Message> queue = microServiceQueues.get(m);
        synchronized (queue) {
            Message message = queue.take(); 
            while (message == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore interrupted status
                }
            }
            return message;
        }		
	}

}
