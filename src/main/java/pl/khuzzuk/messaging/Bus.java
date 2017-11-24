package pl.khuzzuk.messaging;

import org.apache.logging.log4j.Logger;
import pl.khuzzuk.messaging.subscribers.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Bus {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger("MessageLogger");
    private final EventProcessor eventProcessor;
    private final Map<Enum<? extends Enum<?>>, List<Subscriber>> subscribers;
    private final Map<Enum<? extends Enum<?>>, List<RequestSubscriber>> requestSubscribers;
    private final Map<Enum<? extends Enum<?>>, List<ContentSubscriber>> contentSubscribers;
    private final Map<Enum<? extends Enum<?>>, List<TransformerSubscriber>> transformerSubscribers;
    private static boolean logging;

    Bus(EventProcessor eventProcessor,
               Map<Enum<? extends Enum<?>>, List<Subscriber>> subscribers,
               Map<Enum<? extends Enum<?>>, List<RequestSubscriber>> requestSubscribers,
               Map<Enum<? extends Enum<?>>, List<ContentSubscriber>> contentSubscribers,
               Map<Enum<? extends Enum<?>>, List<TransformerSubscriber>> transformerSubscribers) {
        this.eventProcessor = eventProcessor;
        this.subscribers = subscribers;
        this.requestSubscribers = requestSubscribers;
        this.contentSubscribers = contentSubscribers;
        this.transformerSubscribers = transformerSubscribers;
    }

    @SuppressWarnings("unused")
    public static Bus initializeBus() {
        return initializeBus(false);
    }

    @SuppressWarnings("WeakerAccess")
    public static Bus initializeBus(boolean enableLogging) {
        return initializeBus(enableLogging, 3);
    }

    @SuppressWarnings("WeakerAccess")
    public static Bus initializeBus(boolean enableLogging, int threads) {
        Map<Enum<? extends Enum<?>>, List<Subscriber>> subscribers = new HashMap<>();
        Map<Enum<? extends Enum<?>>, List<RequestSubscriber>> requestSubscribers = new HashMap<>();
        Map<Enum<? extends Enum<?>>, List<ContentSubscriber>> contentSubscribers = new HashMap<>();
        Map<Enum<? extends Enum<?>>, List<TransformerSubscriber>> transformerSubscribers = new HashMap<>();
        EventProcessor eventProcessor = new EventProcessor(subscribers, contentSubscribers,
                requestSubscribers, transformerSubscribers, Executors.newFixedThreadPool(threads));
        logging = enableLogging;
        return new Bus(eventProcessor, subscribers, requestSubscribers, contentSubscribers, transformerSubscribers);
    }

    @SuppressWarnings("unused")
    public void unSubscribe(Object subscriber, Enum<? extends Enum<?>> msgType) {
        unSubscribe(subscriber, msgType, subscribers);
        unSubscribe(subscriber, msgType, requestSubscribers);
        unSubscribe(subscriber, msgType, contentSubscribers);
        unSubscribe(subscriber, msgType, transformerSubscribers);
    }

    private void unSubscribe(Object subscriber, Enum<? extends Enum<?>> msgType, Map<Enum<? extends Enum<?>>, ? extends List> subscribers) {
        List list = subscribers.get(msgType);
        if (list != null) {
            list.remove(subscriber);
            if (list.isEmpty()) {
                subscribers.remove(msgType);
            }
        }
    }

    @SuppressWarnings("unused")
    public void closeBus() {
        eventProcessor.close();
    }

    @SuppressWarnings("unused")
    public void removeAllActionsFor(Enum<? extends Enum<?>> topic) {
        subscribers.remove(topic);
        contentSubscribers.remove(topic);
        requestSubscribers.remove(topic);
        transformerSubscribers.remove(topic);
    }

    @SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
    public Object setReaction(Enum<? extends Enum<?>> topic, Action action) {
        Subscriber subscriber = getSubscriber(topic, action);
        subscribers.computeIfAbsent(topic, key -> new ArrayList<>()).add(subscriber);
        return subscriber;
    }

    @SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
    public <T> Object setReaction(Enum<? extends Enum<?>> topic, Consumer<T> consumer) {
        ContentSubscriber contentSubscriber = getContentSubscriber(topic, consumer);
        contentSubscribers.computeIfAbsent(topic, key -> new ArrayList<>()).add(contentSubscriber);
        return contentSubscriber;
    }

    @SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
    public Object setResponse(Enum<? extends Enum<?>> topic, Action reaction) {
        RequestSubscriber requestSubscriber = getRequestSubscriber(topic, reaction);
        requestSubscribers.computeIfAbsent(topic, key -> new ArrayList<>()).add(requestSubscriber);
        return requestSubscriber;
    }

    @SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
    public <T> Object setResponse(Enum<? extends Enum<?>> topic, Supplier<T> supplier) {
        ProducerSubscriber producerSubscriber = getRequestProducerSubscriber(topic, supplier);
        requestSubscribers.computeIfAbsent(topic, key -> new ArrayList<>()).add(producerSubscriber);
        return producerSubscriber;
    }

    @SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
    public <V, R> Object setResponse(Enum<? extends Enum<?>> topic, Function<V, R> resolver) {
        TransformerSubscriber transformerSubscriber = getRequestContentSubscriber(topic, resolver);
        transformerSubscribers.computeIfAbsent(topic, key -> new ArrayList<>()).add(transformerSubscriber);
        return transformerSubscriber;
    }

    @SuppressWarnings("unused")
    public Object setGuiReaction(Enum<? extends Enum<?>> topic, Action action) {
        return getGuiSubscriber(topic, action);
    }

    @SuppressWarnings("unused")
    public <T> Object setGuiReaction(Enum<? extends Enum<?>> topic, Consumer<T> consumer) {
        return getGuiContentSubscriber(topic, consumer);
    }

    @SuppressWarnings("unused")
    public <T, R> Object setGuiResponse(Enum<? extends Enum<?>> topic, Function<T, R> responseResolver) {
        return getGuiRequestContentSubscriber(topic, responseResolver);
    }

    @SuppressWarnings("unused")
    public <T> Object setGuiResponse(Enum<? extends Enum<?>> topic, Supplier<T> supplier) {
        return getGuiRequestContentSubscriber(topic, supplier);
    }

    @SuppressWarnings("WeakerAccess")
    public void send(Enum<? extends Enum<?>> communicate) {
        eventProcessor.processEvent(communicate);
    }

    @SuppressWarnings("unused")
    public <T> void send(Enum<? extends Enum<?>> topic, T content) {
        eventProcessor.processContent(topic, content);
    }

    @SuppressWarnings("unused")
    public <T> void send(Enum<? extends Enum<?>> topic, Enum<? extends Enum<?>> responseTopic, T content) {
        eventProcessor.processRequestWithContent(topic, responseTopic, content);
    }

    @SuppressWarnings("unused")
    public <T> void send(Enum<? extends Enum<?>> topic,
                         Enum<? extends Enum<?>> responseTopic,
                         T content,
                         Enum<? extends Enum<?>> errorTopic) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unused")
    public void sendCommunicate(Enum<? extends Enum<?>> communicate, Enum<? extends Enum<?>> responseTopic) {
        eventProcessor.processRequest(communicate, responseTopic);
    }

    @SuppressWarnings("unused")
    public void sendCommunicate(Enum<? extends Enum<?>> communicate,
                                Enum<? extends Enum<?>> responseTopic,
                                Enum<? extends Enum<?>> errorTopic) {
        throw new UnsupportedOperationException();
    }

    private Subscriber getSubscriber(Enum<? extends Enum> topic, Action reaction) {
        CommunicateSubscriber subscriber = new CommunicateSubscriber();
        subscriber.setMessageType(topic);
        subscriber.setAction(reaction);
        return subscriber;
    }

    private <T> ContentSubscriber getContentSubscriber(Enum<? extends Enum> topic, Consumer<T> consumer) {
        BagSubscriber bagSubscriber = new BagSubscriber(topic);
        bagSubscriber.setConsumer(consumer);
        return bagSubscriber;
    }

    private RequestSubscriber getRequestSubscriber(Enum<? extends Enum> topic, Action reaction) {
        RequestCommunicateSubscriber subscriber = new RequestCommunicateSubscriber(this);
        subscriber.setAction(reaction);
        subscriber.setMessageType(topic);
        return subscriber;
    }

    private Subscriber getGuiSubscriber(Enum<? extends Enum> topic, Action action) {
        GuiCommunicateSubscriber subscriber = new GuiCommunicateSubscriber();
        subscriber.setMessageType(topic);
        subscriber.setAction(action);
        return subscriber;
    }

    private <T> ContentSubscriber getGuiContentSubscriber(Enum<? extends Enum> topic, Consumer<T> consumer) {
        GuiContentSubscriber subscriber = new GuiContentSubscriber(topic);
        subscriber.setConsumer(consumer);
        return subscriber;
    }

    private <T, R> TransformerSubscriber getGuiRequestContentSubscriber(
            Enum<? extends Enum> topic, Function<T, R> responseResolver) {
        GuiRequestBagSubscriber subscriber = new GuiRequestBagSubscriber(this, topic);
        subscriber.setResponseResolver(responseResolver);
        return subscriber;
    }

    private <T> ProducerSubscriber getGuiRequestContentSubscriber(
            Enum<? extends Enum<?>> topic, Supplier<T> supplier) {
        GuiRequestProducerSubscriber subscriber = new GuiRequestProducerSubscriber(this);
        subscriber.setMessageType(topic);
        subscriber.setResponseProducer(supplier);
        return subscriber;
    }

    private <V> ProducerSubscriber getRequestProducerSubscriber(
            Enum<? extends Enum<?>> topic, Supplier<V> supplier) {
        RequestProducerSubscriber subscriber = new RequestProducerSubscriber(this);
        subscriber.setMessageType(topic);
        subscriber.setResponseProducer(supplier);
        return subscriber;
    }

    private <V, R> TransformerSubscriber getRequestContentSubscriber(
            Enum<? extends Enum<?>> topic, Function<V, R> responseResolver) {
        RequestBagSubscriber subscriber = new RequestBagSubscriber(this, topic);
        subscriber.setMessageType(topic);
        subscriber.setResponseResolver(responseResolver);
        return subscriber;
    }
}
