package pl.khuzzuk.messaging;

class BagSubscriber<T, M extends BagMessage<T>> extends AbstractContentSubscriber<T, M> {
    public BagSubscriber(String msgType) {
        setMessageType(msgType);
    }
}
