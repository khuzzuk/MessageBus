package pl.khuzzuk.messaging.subscribers;

import pl.khuzzuk.messaging.Action;
import pl.khuzzuk.messaging.Bus;
import pl.khuzzuk.messaging.Message;

abstract class AbstractSubscriber<T extends Message> implements Subscriber<T> {
    private Bus bus;
    private String messageType;
    private Action action;

    @Override
    public void subscribe() {
        if (messageType == null) throw new IllegalStateException("No message type set for " + this);
        bus.subscribe(this, messageType);
    }

    @Override
    public void receive(T message) {
        action.resolve();
    }

    @Override
    public void unSubscribe() {
        bus.unSubscribe(this, messageType);
    }

    public String toString() {
        return "AbstractSubscriber(messageType=" + this.messageType + ")";
    }

    public Bus getBus() {
        return this.bus;
    }

    public String getMessageType() {
        return this.messageType;
    }

    public Action getAction() {
        return this.action;
    }

    public void setBus(Bus bus) {
        this.bus = bus;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public void setAction(Action action) {
        this.action = action;
    }
}
