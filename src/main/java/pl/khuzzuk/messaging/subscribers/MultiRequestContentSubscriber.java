package pl.khuzzuk.messaging.subscribers;

import pl.khuzzuk.messaging.messages.RequestBagMessage;

import java.util.function.Function;

public interface MultiRequestContentSubscriber extends MultiContentSubscriber<RequestBagMessage<Object>>, RequestContentSubscriber<Object, Object> {
    void subscribe(String msgType, Function responseResolver);
}
