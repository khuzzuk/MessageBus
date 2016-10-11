package pl.khuzzuk.messaging;

class BagSubscriber<T> extends AbstractContentSubscriber<T> {
    public BagSubscriber(String msgType) {
        setMessageType(msgType);
    }
}
