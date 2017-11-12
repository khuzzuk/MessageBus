package pl.khuzzuk.messaging.subscribers;

import pl.khuzzuk.messaging.Action;
import pl.khuzzuk.messaging.Message;

public interface Subscriber<T extends Message> {
    void receive(T message);
    @SuppressWarnings("unused")
    void subscribe();
    @SuppressWarnings("unused")
    void setMessageType(String messageType);
    @SuppressWarnings("unused")
    String getMessageType();
    @SuppressWarnings("unused")
    void setAction(Action action);
    void unSubscribe();
}
