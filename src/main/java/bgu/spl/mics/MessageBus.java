package bgu.spl.mics;

/**
 * The message-bus is a shared object used for communication between
 * micro-services.
 * It should be implemented as a thread-safe singleton.
 * The message-bus implementation must be thread-safe as
 * it is shared between all the micro-services in the system.
 * You must not alter any of the given methods of this interface. 
 * You cannot add methods to this interface.
 */
/*@ INV: 1. for each (MicroService m: subscribers.keySet()) subscribers.get(m) != null;
         2. for each (<Class<? extends Message> mes: messageHandlerMap.keySet()) {
                    for each (MicroService ser: messageHandlerMap.get(m)) ser != null };
         3. messageHandlerMap.keySet() == messageRoundRobin.keySet();
         4. All data structures are thread-safe
* */
public interface MessageBus {

    /**
     * Subscribes {@code m} to receive {@link Event}s of type {@code type}.
     * <p>
     * @param <T>  The type of the result expected by the completed event.
     * @param type The type to subscribe to,
     * @param m    The subscribing micro-service.
     */
    /*
    preConditions: type != null && m != null && m is registered
    postCondition: pre(messageHandlersMap.get(e).size()) + 1 = post(messageHandlersMap.get(e).size())
    */
    <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m);

    /**
     * Subscribes {@code m} to receive {@link Broadcast}s of type {@code type}.
     * <p>
     * @param type 	The type to subscribe to.
     * @param m    	The subscribing micro-service.
     */
    /*
     preConditions: type != null && m != null && m is registered
     postCondition:  postCondition: pre(messageHandlersMap.get(e).size()) + 1 = post(messageHandlersMap.get(e).size())
     */
    void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m);

    /**
     * Notifies the MessageBus that the event {@code e} is completed and its
     * result was {@code result}.
     * When this method is called, the message-bus will resolve the {@link Future}
     * object associated with {@link Event} {@code e}.
     * <p>
     * @param <T>    The type of the result expected by the completed event.
     * @param e      The completed event.
     * @param result The resolved result of the completed event.
     */
    /*
     preConditions: e != null
     postCondition:  e.isDone() == true
     */
    <T> void complete(Event<T> e, T result);

    /**
     * Adds the {@link Broadcast} {@code b} to the message queues of all the
     * micro-services subscribed to {@code b.getClass()}.
     * <p>
     * @param b 	The message to added to the queues.
     */
       /*
     preConditions: b != null && messageHandlersMap.get(b) != null && messageHandlersMap.get(b).size() > 0
     postCondition:  for (LinkedBlockingQueue<Message> q: subscribers.values()) {pre(q.size()) + 1 == post(q.size())}
     */
    void sendBroadcast(Broadcast b);

    /**
     * Adds the {@link Event} {@code e} to the message queue of one of the
     * micro-services subscribed to {@code e.getClass()} in a round-robin
     * fashion. This method should be non-blocking.
     * <p>
     * @param <T>    	The type of the result expected by the event and its corresponding future object.
     * @param e     	The event to add to the queue.
     * @return {@link Future<T>} object to be resolved once the processing is complete,
     * 	       null in case no micro-service has subscribed to {@code e.getClass()}.
     */
    /*
     preConditions: e != null && e.getFuture() == null
     postCondition:  pre(messageHandlersMap.get(e) != null && messageHandlersMap.get(e).size() > 0) ?
                     the event entered the queue in subscribers which corresponds to the correct MicroService
                     according to messageRoundRobin entry which indicates which instance should get the message.
     postCondition: e.getFuture() != null
    */
    <T> Future<T> sendEvent(Event<T> e);

    /**
     * Allocates a message-queue for the {@link MicroService} {@code m}.
     * <p>
     * @param m the micro-service to create a queue for.
     */
       /*
     preConditions: m != null && m is registered && subscribers.get(m) == null
     postCondition: subscribers.get(m) != null && subscribers.get(m).isEmpty() == true
     */
    void register(MicroService m);

    /**
     * Removes the message queue allocated to {@code m} via the call to
     * {@link #register(bgu.spl.mics.MicroService)} and cleans all references
     * related to {@code m} in this message-bus. If {@code m} was not
     * registered, nothing should happen.
     * <p>
     * @param m the micro-service to unregister.
     */
       /*
     preConditions:  m != null && m is registered
     postCondition: for (List<MicroService> lst: messageHandlersMap.values()) {lst.isEmpty()}
     */
    void unregister(MicroService m);

    /**
     * Using this method, a <b>registered</b> micro-service can take message
     * from its allocated queue.
     * This method is blocking meaning that if no messages
     * are available in the micro-service queue it
     * should wait until a message becomes available.
     * The method should throw the {@link IllegalStateException} in the case
     * where {@code m} was never registered.
     * <p>
     * @param m The micro-service requesting to take a message from its message
     *          queue.
     * @return The next message in the {@code m}'s queue (blocking).
     * @throws InterruptedException if interrupted while waiting for a message
     *                              to became available.
     */
       /*
     preConditions: m != null && m is registered
     postCondition: pre(subscribers.get(m).size()) - 1 == post(subscribers.get(m).size())
     */
    Message awaitMessage(MicroService m) throws InterruptedException;
    
}
