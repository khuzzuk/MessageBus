package pl.khuzzuk.messaging;

import java.util.function.Consumer;

interface MultiContentSubscriber<T extends BagMessage> extends MultiSubscriber<T> {
    <V> void subscribe(String msgType, Consumer<V> consumer);
}
