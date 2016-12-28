package pl.khuzzuk.messaging;

import lombok.Getter;

import java.util.function.Supplier;

class RequestCommunicateProducerSubscriber<T> extends AbstractSubscriber<RequestMessage> implements RequestProducerSubscriber<T> {
    @Getter
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
}
