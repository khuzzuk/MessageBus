package pl.khuzzuk.messaging;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

abstract class AbstractMultiSubscriber extends AbstractSubscriber implements MultiSubscriber<Message> {
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
    public void receive(Message message) {
        messages.get(message.getType()).stream().forEach(Reactor::resolve);
    }
}
