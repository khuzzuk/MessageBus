package pl.khuzzuk.messaging.publisher;

import pl.khuzzuk.messaging.messages.ContentMessage;

abstract class AbstractMultiContentPublisher
        extends AbstractBagPublisher<Object> implements MultiContentPublisher {
    @SuppressWarnings("unchecked")
    @Override
    public void publish(Object content, String msgType) {
        getBus().publish(new ContentMessage().setMessage(content).setType(msgType));
    }
}
