package pl.khuzzuk.messaging;

import java.util.function.Function;

interface MultiRequestContentSubscriber extends MultiContentSubscriber<RequestBagMessage<Object>>, RequestContentSubscriber<Object, Object> {
    void subscribe(String msgType, Function responseResolver);
}
