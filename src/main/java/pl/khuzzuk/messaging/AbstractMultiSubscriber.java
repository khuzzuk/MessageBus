package pl.khuzzuk.messaging;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

abstract class AbstractMultiSubscriber<T extends Message> extends AbstractSubscriber<T> implements MultiSubscriber<T> {
    private MultiValuedMap<String, Reactor> messages;
    @Override
    public void subscribe(String msgType, Reactor reactor) {
        if (messages == null) {
            messages = new HashSetValuedHashMap<>();
        }
        messages.put(msgType, reactor);
        getBus().subscribe(this, msgType);
    }

    @Override
    public void unSubscribe(String msgType) {
        getBus().unSubscribe(this, msgType);
    }

    @Override
    public void receive(T message) {
        messages.get(message.getType()).forEach(Reactor::resolve);
    }
}
