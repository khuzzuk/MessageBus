package pl.khuzzuk.messaging.publisher;

import pl.khuzzuk.messaging.messages.BagMessage;

public interface BagPublisher<T> extends Publisher<BagMessage<T>> {
    void publish(T content, String msgType);
}
