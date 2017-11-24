package pl.khuzzuk.messaging.subscribers;

import pl.khuzzuk.messaging.Bus;

public class RequestCommunicateSubscriber extends AbstractSubscriber implements RequestSubscriber {
    private Bus bus;

    public RequestCommunicateSubscriber(Bus bus) {
        this.bus = bus;
    }

    @Override
    public void receive(Enum<? extends Enum> responseTopic) {
        super.receive();
        bus.send(responseTopic);
    }

    public Bus getBus() {
        return bus;
    }
}
