package pl.khuzzuk.messaging;

interface BagPublisher<T> extends Publisher<BagMessage<T>> {
    void publish(T content, String msgType);
}
