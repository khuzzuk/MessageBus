package pl.khuzzuk.messaging;

class RequestCommunicateSubscriber extends AbstractSubscriber<RequestMessage> {
    @Override
    public void receive(RequestMessage message) {
        super.receive(message);
        getBus().publish(new CommunicateMessage().setType(message.getResponseType()));
    }
}