package pl.khuzzuk.messaging.subscribers;

import java.util.function.Consumer;

public interface ContentSubscriber extends Subscriber {
    <T> void receive(T content);

    <T> void setConsumer(Consumer<T> consumer);
}
