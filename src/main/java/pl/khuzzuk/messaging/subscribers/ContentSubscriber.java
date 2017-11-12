package pl.khuzzuk.messaging.subscribers;

import pl.khuzzuk.messaging.messages.BagMessage;

import java.util.function.Consumer;

public interface ContentSubscriber<T, M extends BagMessage<T>> extends Subscriber<M> {
    void receive(T content);

    void setConsumer(Consumer<T> consumer);
}
