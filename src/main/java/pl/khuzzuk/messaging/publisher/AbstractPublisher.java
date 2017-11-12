package pl.khuzzuk.messaging.publisher;

import pl.khuzzuk.messaging.Bus;
import pl.khuzzuk.messaging.Message;

abstract class AbstractPublisher<T extends Message> implements Publisher<T> {
    private Bus bus;

    @Override
    public void publish(T message) {
        bus.publish(message);
    }

    public void setBus(Bus bus) {
        this.bus = bus;
    }
}
