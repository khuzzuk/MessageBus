package pl.khuzzuk.messaging.subscribers;

import pl.khuzzuk.messaging.messages.CommunicateMessage;
import pl.khuzzuk.messaging.messages.RequestMessage;

public class RequestCommunicateSubscriber extends AbstractSubscriber<RequestMessage> {
    @Override
    public void receive(RequestMessage message) {
        super.receive(message);
        getBus().publish(new CommunicateMessage().setType(message.getResponseType()));
    }
}
