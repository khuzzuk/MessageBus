package pl.khuzzuk.messaging;

public interface MultiSubscriber<T extends Message> extends Subscriber<T> {
    void subscribe(String msgType, Reactor reactor);

    void unSubscribe(String msgType);
}
