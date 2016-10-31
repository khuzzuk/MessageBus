package pl.khuzzuk.messaging;

import lombok.*;

@ToString(exclude = {"bus", "reactor"})
abstract class AbstractSubscriber<T extends Message> implements Subscriber<T> {
    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    private Bus bus;
    @Setter
    @Getter
    @NonNull
    private String messageType;
    @Setter
    @Getter(AccessLevel.PACKAGE)
    @NonNull
    private Reactor reactor;

    @Override
    public void subscribe() {
        if (messageType == null) throw new IllegalStateException("No message type set for " + this);
        bus.subscribe(this, messageType);
    }

    @Override
    public void receive(T message) {
        reactor.resolve();
    }

    @Override
    public void unSubscribe() {
        bus.unSubscribe(this, messageType);
    }
}
