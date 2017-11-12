package pl.khuzzuk.messaging.subscribers;

import org.apache.logging.log4j.Logger;
import pl.khuzzuk.messaging.Action;
import pl.khuzzuk.messaging.Bus;
import pl.khuzzuk.messaging.messages.BagMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

abstract class AbstractMultiContentSubscriber<T extends BagMessage> implements MultiContentSubscriber<T> {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger("MessageLogger");
    private Bus bus;
    private String messageType;
    private Map<String, List<Consumer>> consumers;
    private Map<String, List<Action>> actions;
    @SuppressWarnings("unchecked")
    @Override
    public void receive(T message) {
        try {
            consumers.get(message.getType()).forEach(c -> c.accept(message.getMessage()));
        } catch (ClassCastException e) {
            log.error("Wrong type of Message for Consumer: " + consumers.get(message.getType()) +
                    " and message type: " + message.getType());
            e.printStackTrace();
        }
        actions.get(message.getType()).forEach(Action::resolve);
    }

    @Override
    public <V> void subscribe(String msgType, Consumer<V> consumer) {
        assureInit();
        consumers.computeIfAbsent(msgType, k -> new ArrayList<>());
        consumers.get(msgType).add(consumer);
        bus.subscribe(this, msgType);
    }

    @Override
    public void subscribe(String msgType, Action action) {
        assureInit();
        actions.computeIfAbsent(msgType, k -> new ArrayList<>());
        actions.get(msgType).add(action);
        bus.subscribe(this, msgType);
    }

    @Override
    public void unSubscribe(String msgType) {
        assureInit();
        bus.unSubscribe(this, messageType);
        consumers.remove(msgType);
        actions.remove(msgType);
    }

    @Override
    public void subscribe() {
        bus.subscribe(this, messageType);
    }

    @Override
    public void unSubscribe() {
        bus.unSubscribe(this, messageType);
    }

    void assureInit() {
        if (consumers == null) {
            consumers = new HashMap<>();
        }
        if (actions == null) {
            actions= new HashMap<>();
        }
    }

    public void setConsumer(Consumer consumer) {
        assureInit();
        consumers.computeIfAbsent(messageType, k -> new ArrayList<>());
        consumers.get(messageType).add(consumer);
    }

    @Override
    public void setAction(Action action) {
        assureInit();
        actions.computeIfAbsent(messageType, k -> new ArrayList<>());
        actions.get(messageType).add(action);
    }

    @SuppressWarnings("unchecked")
    void receive(Object content) {
        consumers.get(messageType).forEach(c -> c.accept(content));
        actions.get(messageType).forEach(Action::resolve);
    }

    public Bus getBus() {
        return this.bus;
    }

    public String getMessageType() {
        return this.messageType;
    }

    public void setBus(Bus bus) {
        this.bus = bus;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
}
