package pl.khuzzuk.messaging.subscribers;

import pl.khuzzuk.messaging.Action;
import pl.khuzzuk.messaging.Bus;
import pl.khuzzuk.messaging.messages.BagMessage;

import java.util.function.Consumer;

abstract class AbstractContentSubscriber<T, M extends BagMessage<T>> implements ContentSubscriber<T, M> {
    private Bus bus;
    Consumer<T> consumer;
    private String messageType;
    Action action;

    @Override
    public void receive(M message) {
        if (consumer != null) {
            receive(message.getMessage());
        } else if (action != null) {
            action.resolve();
        } else {
            throw new IllegalStateException("cannot handle " +
                    message.toString() +
                    ", neither consumer nor action was set.");
        }
    }

    @Override
    public void subscribe() {
        if (messageType == null) throw new IllegalStateException("No message type set for " + this);
        bus.subscribe(this, messageType);
    }

    @Override
    public void receive(T content) {
        consumer.accept(content);
    }

    @Override
    public void unSubscribe() {
        bus.unSubscribe(this, getMessageType());
    }

    public String toString() {
        return "AbstractContentSubscriber(messageType=" + this.messageType + ")";
    }

    public Bus getBus() {
        return this.bus;
    }

    public String getMessageType() {
        return this.messageType;
    }

    public void setBus(Bus bus) {
        this.bus = bus;
    }

    public void setConsumer(Consumer<T> consumer) {
        this.consumer = consumer;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public void setAction(Action action) {
        this.action = action;
    }
}
