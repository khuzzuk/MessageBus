package pl.khuzzuk.messaging.subscribers;

public class BagSubscriber extends AbstractContentSubscriber {
    public BagSubscriber(Enum<? extends Enum> msgType) {
        setMessageType(msgType);
    }
}
