package pl.khuzzuk.messaging.subscribers;

import pl.khuzzuk.messaging.messages.ContentMessage;
import pl.khuzzuk.messaging.messages.RequestMessage;

import java.util.function.Supplier;

public class RequestCommunicateProducerSubscriber<T> extends AbstractSubscriber<RequestMessage> implements RequestProducerSubscriber<T> {
    Supplier<T> supplier;
    @Override
    public RequestProducerSubscriber<T> setResponseProducer(Supplier<T> supplier) {
        this.supplier = supplier;
        return this;
    }

    @Override
    public void receive(RequestMessage message) {
        getBus().publish(new ContentMessage<>().setType(message.getResponseType()).setMessage(supplier.get()));
    }

    public Supplier<T> getSupplier() {
        return this.supplier;
    }
}
