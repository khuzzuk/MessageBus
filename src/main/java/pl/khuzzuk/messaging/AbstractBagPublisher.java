package pl.khuzzuk.messaging;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

abstract class AbstractBagPublisher<T> implements BagPublisher<T> {
    @Getter(value = AccessLevel.PACKAGE)
    @Setter(value = AccessLevel.PACKAGE)
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
}
