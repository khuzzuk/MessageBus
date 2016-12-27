package pl.khuzzuk.messaging;

interface MultiSubscriber<T extends Message> extends Subscriber<T> {
    void subscribe(String msgType, Reactor reactor);

    void unSubscribe(String msgType);
}
