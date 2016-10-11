package pl.khuzzuk.messaging;

import java.util.function.Consumer;

public interface MultiContentSubscriber extends MultiSubscriber<BagMessage> {
    <T> void subscribe(String msgType, Consumer<T> consumer);
}
