package pl.khuzzuk.messaging.subscribers;

import pl.khuzzuk.messaging.Bus;

public class RequestMessageSubscriber extends AbstractSubscriber implements RequestSubscriber {
    private Bus bus;

    public RequestMessageSubscriber(Bus bus) {
        this.bus = bus;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void receive(Enum<? extends Enum> responseTopic) {
        super.receive();
        bus.message(responseTopic).send();
    }

    public Bus getBus() {
        return bus;
    }
}
