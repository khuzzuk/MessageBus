package pl.khuzzuk.messaging;

import java.util.function.Consumer;

public interface ContentSubscriber<T> extends Subscriber<BagMessage<T>> {
    void receive(T content);

    void setConsumer(Consumer<T> consumer);
}
