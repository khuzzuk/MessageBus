package pl.khuzzuk.messaging;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Log4j2(topic = "MessageLogger")
abstract class AbstractMultiContentSubscriber<T extends BagMessage> implements MultiContentSubscriber<T> {
    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    private Bus bus;
    @Getter
    @Setter
    private String messageType;
    private Map<String, List<Consumer>> consumers;
    private Map<String, List<Reactor>> reactors;
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
        reactors.get(message.getType()).forEach(Reactor::resolve);
    }

    @Override
    public <V> void subscribe(String msgType, Consumer<V> consumer) {
        assureInit();
        consumers.computeIfAbsent(msgType, k -> new ArrayList<>());
        consumers.get(msgType).add(consumer);
        bus.subscribe(this, msgType);
    }

    @Override
    public void subscribe(String msgType, Reactor reactor) {
        assureInit();
        reactors.computeIfAbsent(msgType, k -> new ArrayList<>());
        reactors.get(msgType).add(reactor);
        bus.subscribe(this, msgType);
    }

    @Override
    public void unSubscribe(String msgType) {
        assureInit();
        bus.unSubscribe(this, messageType);
        consumers.remove(msgType);
        reactors.remove(msgType);
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
        if (reactors == null) {
            reactors = new HashMap<>();
        }
    }

    public void setConsumer(Consumer consumer) {
        assureInit();
        consumers.computeIfAbsent(messageType, k -> new ArrayList<>());
        consumers.get(messageType).add(consumer);
    }

    @Override
    public void setReactor(Reactor reactor) {
        assureInit();
        reactors.computeIfAbsent(messageType, k -> new ArrayList<>());
        reactors.get(messageType).add(reactor);
    }

    @SuppressWarnings("unchecked")
    public void receive(Object content) {
        consumers.get(messageType).forEach(c -> c.accept(content));
        reactors.get(messageType).forEach(Reactor::resolve);
    }
}
