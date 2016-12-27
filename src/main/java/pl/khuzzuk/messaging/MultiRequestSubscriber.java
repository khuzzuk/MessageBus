package pl.khuzzuk.messaging;

class MultiRequestSubscriber extends AbstractMultiSubscriber<RequestMessage> {
    @Override
    public void receive(RequestMessage message) {
        super.receive(message);
        getBus().publish(new CommunicateMessage().setType(message.getResponseType()));
    }
}
