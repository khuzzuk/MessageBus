package pl.khuzzuk.messaging;

import lombok.*;

import java.util.function.Consumer;

@ToString(exclude = {"bus", "reactor", "consumer"})
abstract class AbstractContentSubscriber<T> implements ContentSubscriber<T> {
    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    private Bus bus;
    @Setter
    Consumer<T> consumer;
    @Setter
    @Getter
    private String messageType;
    @Setter
    private Reactor reactor;

    @Override
    public void receive(BagMessage<T> message) {
        if (consumer != null) {
            receive(message.getMessage());
        } else if (reactor != null) {
            reactor.resolve();
        } else {
            throw new IllegalStateException("cannot handle " +
                    message.toString() +
                    ", neither consumer nor reactor was set.");
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
}
