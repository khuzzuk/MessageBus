package pl.khuzzuk.messaging.subscribers;

import pl.khuzzuk.messaging.Action;
import pl.khuzzuk.messaging.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class AbstractMultiSubscriber<T extends Message> extends AbstractSubscriber<T> implements MultiSubscriber<T> {
    private Map<String, List<Action>> messages;
    @Override
    public void subscribe(String msgType, Action action) {
        if (messages == null) {
            messages = new HashMap<>();
        }
        messages.computeIfAbsent(msgType, k -> new ArrayList<>());
        messages.get(msgType).add(action);
        getBus().subscribe(this, msgType);
    }

    @Override
    public void unSubscribe(String msgType) {
        getBus().unSubscribe(this, msgType);
    }

    @Override
    public void receive(T message) {
        messages.get(message.getType()).forEach(Action::resolve);
    }
}
