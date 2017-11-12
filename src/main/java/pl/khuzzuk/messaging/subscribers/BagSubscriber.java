package pl.khuzzuk.messaging.subscribers;

import pl.khuzzuk.messaging.messages.BagMessage;

public class BagSubscriber<T, M extends BagMessage<T>> extends AbstractContentSubscriber<T, M> {
    public BagSubscriber(String msgType) {
        setMessageType(msgType);
    }
}
