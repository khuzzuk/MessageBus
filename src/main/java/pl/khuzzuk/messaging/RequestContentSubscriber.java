package pl.khuzzuk.messaging;

import java.util.function.Function;

public interface RequestContentSubscriber<T, R> extends ContentSubscriber<T, RequestBagMessage<T>> {
    RequestContentSubscriber<T, R> setResponseResolver(Function<T, R> responseResolver);
}
