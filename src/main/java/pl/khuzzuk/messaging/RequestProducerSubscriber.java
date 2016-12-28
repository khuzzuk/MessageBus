package pl.khuzzuk.messaging;

import java.util.function.Supplier;

interface RequestProducerSubscriber<T> extends Subscriber<RequestMessage> {
    RequestProducerSubscriber<T> setResponseProducer(Supplier<T> supplier);
}
