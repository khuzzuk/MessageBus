package pl.khuzzuk.messaging.subscribers;

import pl.khuzzuk.messaging.Action;
import pl.khuzzuk.messaging.Bus;

abstract class AbstractSubscriber implements Subscriber {
    private Enum<? extends Enum> messageType;
    Action action;

    @Override
    public void receive() {
        action.resolve();
    }

    public String toString() {
        return "Subscriber(messageType=" + this.messageType + ")";
    }

    public Enum<? extends Enum> getMessageType() {
        return this.messageType;
    }

    public void setMessageType(Enum<? extends Enum> messageType) {
        this.messageType = messageType;
    }

    public void setAction(Action action) {
        this.action = action;
    }
}
