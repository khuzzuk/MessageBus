package pl.khuzzuk.messaging.subscribers;

import java.util.function.Supplier;

public interface ProducerSubscriber extends RequestSubscriber {
    @SuppressWarnings("UnusedReturnValue")
    <T> ProducerSubscriber setResponseProducer(Supplier<T> supplier);
}
