package bgu.spl.mics;

public class EventImp<T> implements Event<T> {
    private Future<T> future;
    private final String SenderId;

    public EventImp(String SenderId) {
        this.SenderId = SenderId;
    }
    @Override
    public void setFuture(Future<T> future) {
        this.future = future;
    }

    @Override
    public void resolveFuture(T result) {
        future.resolve(result);
    }

    public Event<T> currentEvent() {
        return this;
    }
    public String getSenderId() {
        return SenderId;
    }
}
