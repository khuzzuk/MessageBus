package pl.khuzzuk.messaging.subscribers;

import pl.khuzzuk.messaging.Bus;

import java.util.function.Supplier;

public class RequestProducerSubscriber extends RequestCommunicateSubscriber implements ProducerSubscriber {
    private Supplier supplier;

    public RequestProducerSubscriber(Bus bus) {
        super(bus);
    }

    @Override
    public void receive(Enum<? extends Enum> responseTopic) {
        getBus().send(responseTopic, supplier.get());
    }

    @Override
    public ProducerSubscriber setResponseProducer(Supplier supplier) {
        this.supplier = supplier;
        return this;
    }
}
