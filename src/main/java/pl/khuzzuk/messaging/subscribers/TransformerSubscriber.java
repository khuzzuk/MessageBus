package pl.khuzzuk.messaging.subscribers;

import java.util.function.Function;

public interface TransformerSubscriber extends ContentSubscriber {
    <T, R> TransformerSubscriber setResponseResolver(Function<T, R> responseResolver);

    <T> void receive(T content, Enum<? extends Enum<?>> responseTopic);
}
