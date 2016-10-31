package pl.khuzzuk.messaging;

import lombok.AccessLevel;
import lombok.Setter;

abstract class AbstractPublisher<T extends Message> implements Publisher<T> {
    @Setter(AccessLevel.PACKAGE)
    private Bus bus;

    @Override
    public void publish(T message) {
        bus.publish(message);
    }
}
