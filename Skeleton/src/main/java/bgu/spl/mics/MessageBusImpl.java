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
	private static class Singleton {
        private static final MessageBusImpl INSTANCE = new MessageBusImpl();
    }

	
	private final ConcurrentHashMap<MicroService, BlockingQueue<Message>> microServiceQueues;
    private final ConcurrentHashMap<Class<? extends Event>, Queue<MicroService>> eventSubscribers;
    private final ConcurrentHashMap<Class<? extends Broadcast>, CopyOnWriteArrayList<MicroService>> broadcastSubscribers;
    private final ConcurrentHashMap<Event<?>, Future<?>> eventFutures;
    private final ReadWriteLock lock;


	private MessageBusImpl(){
		microServiceQueues = new ConcurrentHashMap<>();
		eventSubscribers = new ConcurrentHashMap<>();
		broadcastSubscribers = new ConcurrentHashMap<>();
		eventFutures = new ConcurrentHashMap<>();
		lock = new ReentrantReadWriteLock();
	
	}  //private constructor for the singelton class
	public static MessageBusImpl getBusInstance() {
        return Singleton.INSTANCE;
    } 
	
	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		lock.writeLock().lock();
        try {
            eventSubscribers.putIfAbsent(type, new ConcurrentLinkedQueue<>());
            eventSubscribers.get(type).add(m);
        } finally {
            lock.writeLock().unlock();
        }
    }	

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
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
	public void sendBroadcast(Broadcast b) {
		CopyOnWriteArrayList<MicroService> subscribers = broadcastSubscribers.get(b.getClass());
        if (subscribers != null) {
            for (MicroService m : subscribers) {
                BlockingQueue<Message> queue = microServiceQueues.get(m);
                if (queue != null) {
                    queue.offer(b);
                }
            }
        }
    }
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void register(MicroService m) {
		microServiceQueues.putIfAbsent(m, new LinkedBlockingQueue<>());
    }

	@Override
	public void unregister(MicroService m) {
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
	public Message awaitMessage(MicroService m) throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	

}
