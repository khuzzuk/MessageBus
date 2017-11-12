package pl.khuzzuk.messaging.subscribers;

import pl.khuzzuk.messaging.messages.RequestMessage;

import java.util.function.Supplier;

public interface RequestProducerSubscriber<T> extends Subscriber<RequestMessage> {
    @SuppressWarnings("unused")
    RequestProducerSubscriber<T> setResponseProducer(Supplier<T> supplier);
}
