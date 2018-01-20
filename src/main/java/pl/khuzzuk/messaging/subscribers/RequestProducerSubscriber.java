package pl.khuzzuk.messaging.subscribers;

import pl.khuzzuk.messaging.Bus;

import java.util.function.Supplier;

public class RequestProducerSubscriber extends RequestMessageSubscriber implements ProducerSubscriber {
    private Supplier supplier;

    public RequestProducerSubscriber(Bus bus) {
        super(bus);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void receive(Enum<? extends Enum> responseTopic, Enum<? extends Enum> errorTopic) {
        try {
            Object content = supplier.get();
            getBus().send(responseTopic, content);
        } catch (Throwable t) {
            t.printStackTrace();
            getBus().send(errorTopic);
        }
    }

    @Override
    public ProducerSubscriber setResponseProducer(Supplier supplier) {
        this.supplier = supplier;
        return this;
    }
}
