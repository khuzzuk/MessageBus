package pl.khuzzuk.messaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class AbstractMultiSubscriber<T extends Message> extends AbstractSubscriber<T> implements MultiSubscriber<T> {
    private Map<String, List<Reactor>> messages;
    @Override
    public void subscribe(String msgType, Reactor reactor) {
        if (messages == null) {
            messages = new HashMap<>();
        }
        messages.computeIfAbsent(msgType, k -> new ArrayList<>());
        messages.get(msgType).add(reactor);
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
