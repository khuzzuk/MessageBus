package pl.khuzzuk.messaging.subscribers;

import pl.khuzzuk.messaging.messages.BagMessage;

public class MultiBagSubscriber extends AbstractMultiContentSubscriber<BagMessage> {
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
