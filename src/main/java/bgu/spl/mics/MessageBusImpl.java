package bgu.spl.mics;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only one public method (in addition to getters which can be public solely for unit testing) may be added to this class
 * All other methods and members you add the class must be private.
 */

public class MessageBusImpl implements MessageBus {
    private final ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>> subscribers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<? extends Message>, List<MicroService>> messageHandlersMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<? extends Message>, AtomicInteger> messageRoundRobin = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock lockreadwrite = new ReentrantReadWriteLock();

    private MessageBusImpl() {
    }



    private static class MessageBusHolder {
        private static MessageBusImpl instance = new MessageBusImpl();
    }

    //pre:
    @Override
    public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
        SubscribeBase(type, m);
    }

    private void SubscribeBase(Class<? extends Message> type, MicroService m) {
        synchronized (this.messageHandlersMap) {
            if (!messageHandlersMap.containsKey(type)) {
                messageHandlersMap.put(type, new CopyOnWriteArrayList<>());
                messageRoundRobin.put(type, new AtomicInteger(0));
            }
            this.messageHandlersMap.get(type).add(m);
        }
    }

    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
        SubscribeBase(type, m);
    }

    @Override
    public <T> void complete(Event<T> e, T result) {
        e.resolveFuture(result);
    }

    @Override
    public void sendBroadcast(Broadcast b) {
        lockreadwrite.writeLock().lock();
        try {
            List<MicroService> services = messageHandlersMap.get(b.getClass());
            if (services != null) {
                services.forEach(service -> {
                    try {
                        subscribers.get(service).put(b);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        System.out.println("Broadcast interrupted for service: " + service.getName());
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Broadcast failed: " + e.getMessage());
        } finally {
            lockreadwrite.writeLock().unlock();
        }
    }



    @Override
    public <T> Future<T> sendEvent(Event<T> e) {
        lockreadwrite.readLock().lock();
        try {
            if (messageHandlersMap.isEmpty()) return null;

            Future<T> future = new Future<>();
            e.setFuture(future);

            List<MicroService> services = messageHandlersMap.get(e.getClass());
            AtomicInteger index = messageRoundRobin.get(e.getClass());
            int serviceIdx = index.getAndIncrement();
            MicroService service = services.get(serviceIdx % services.size());

            subscribers.get(service).put(e);
            return future;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            lockreadwrite.readLock().unlock();
        }
    }


    @Override
    public void register(MicroService m) {
        subscribers.put(m, new LinkedBlockingQueue<>());
    }

    @Override
    public void unregister(MicroService m) {
        lockreadwrite.writeLock().lock();
        try {
            subscribers.remove(m);
            messageHandlersMap.values().forEach(handlers -> handlers.remove(m));
        } finally {
            lockreadwrite.writeLock().unlock();
        }
    }

    @Override
    public Message awaitMessage(MicroService m) throws InterruptedException {
        return subscribers.get(m).take();
    }
    public static MessageBusImpl getInstance() {
        return MessageBusHolder.instance;
    }

    //Only for unit tests
    public synchronized void clearInstance() {
        MessageBusHolder.instance.messageHandlersMap.clear();
        MessageBusHolder.instance.subscribers.clear();
        MessageBusHolder.instance.messageRoundRobin.clear();
    }

    // Only for unit testing
    public ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>> getSubscribers (){return subscribers;}
    //Only for unit testing
    public int getRoundRobinIndexByService(Class< ? extends Message> m){
        return messageRoundRobin.get(m).get();
    }
}
