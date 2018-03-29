package pl.khuzzuk.messaging.subscribers;

import java.util.function.Supplier;

import pl.khuzzuk.messaging.Bus;

public class RequestProducerSubscriber extends RequestMessageSubscriber implements ProducerSubscriber {
    private Supplier supplier;

    public RequestProducerSubscriber(Bus bus) {
        super(bus);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void receive(Enum<? extends Enum> responseTopic) {
        Object content = supplier.get();
        getBus().message(responseTopic).withContent(content).send();
    }

    @Override
    public ProducerSubscriber setResponseProducer(Supplier supplier) {
        this.supplier = supplier;
        return this;
    }
}
