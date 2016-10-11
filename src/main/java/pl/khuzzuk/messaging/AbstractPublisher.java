package pl.khuzzuk.messaging;

import lombok.AccessLevel;
import lombok.Setter;

abstract class AbstractPublisher implements Publisher<Message> {
    @Setter(AccessLevel.PACKAGE)
    private Bus bus;

    @Override
    public void publish(Message message) {
        bus.publish(message);
    }
}
