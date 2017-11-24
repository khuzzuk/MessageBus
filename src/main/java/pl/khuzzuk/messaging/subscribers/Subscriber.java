package pl.khuzzuk.messaging.subscribers;

import pl.khuzzuk.messaging.Action;

public interface Subscriber {
    void receive();
    @SuppressWarnings("unused")
    void setMessageType(Enum<? extends Enum> messageType);
    @SuppressWarnings("unused")
    Enum<? extends Enum> getMessageType();
    @SuppressWarnings("unused")
    void setAction(Action action);
}
