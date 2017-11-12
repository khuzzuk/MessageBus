package pl.khuzzuk.messaging.publisher;

import pl.khuzzuk.messaging.Bus;
import pl.khuzzuk.messaging.messages.BagMessage;
import pl.khuzzuk.messaging.messages.ContentMessage;

abstract class AbstractBagPublisher<T> implements BagPublisher<T> {
    private Bus bus;
    @Override
    public void publish(T content, String msgType) {
        BagMessage<T> message = new ContentMessage<>();
        message.setMessage(content);
        message.setType(msgType);
        publish(message);
    }

    @Override
    public void publish(BagMessage<T> message) {
        bus.publish(message);
    }

    public Bus getBus() {
        return this.bus;
    }

    public void setBus(Bus bus) {
        this.bus = bus;
    }
}
