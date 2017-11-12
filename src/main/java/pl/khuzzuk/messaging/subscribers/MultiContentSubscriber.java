package pl.khuzzuk.messaging.subscribers;

import pl.khuzzuk.messaging.messages.BagMessage;

import java.util.function.Consumer;

public interface MultiContentSubscriber<T extends BagMessage> extends MultiSubscriber<T> {
    <V> void subscribe(String msgType, Consumer<V> consumer);
}
