package pl.khuzzuk.messaging.subscribers;

import pl.khuzzuk.messaging.Bus;

public class RequestMessageSubscriber extends AbstractSubscriber implements RequestSubscriber {
    private Bus bus;

    public RequestMessageSubscriber(Bus bus) {
        this.bus = bus;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void receive(Enum<? extends Enum> responseTopic, Enum<? extends Enum> errorTopic) {
        try {
            super.receive();
        } catch (Throwable t) {
            t.printStackTrace();
            if (errorTopic != null) {
                bus.send(errorTopic);
            }
            return;
        }
        bus.send(responseTopic);
    }

    public Bus getBus() {
        return bus;
    }
}
