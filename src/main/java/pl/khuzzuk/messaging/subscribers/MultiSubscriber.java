package pl.khuzzuk.messaging.subscribers;

import pl.khuzzuk.messaging.Action;
import pl.khuzzuk.messaging.Message;

@SuppressWarnings("unused")
public interface MultiSubscriber<T extends Message> extends Subscriber<T> {
    void subscribe(String msgType, Action action);

    void unSubscribe(String msgType);
}
