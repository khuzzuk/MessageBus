package pl.khuzzuk.messaging;

import org.apache.logging.log4j.Logger;
import pl.khuzzuk.messaging.subscribers.*;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Bus<T extends Enum<T>> {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger("MessageLogger");
    private Class<T> enumType;
    private final EventProcessor<T> eventProcessor;
    private final Map<T, List<Subscriber>> subscribers;
    private final Map<T, List<RequestSubscriber>> requestSubscribers;
    private final Map<T, List<ContentSubscriber>> contentSubscribers;
    private final Map<T, List<TransformerSubscriber>> transformerSubscribers;
    private boolean logging;

    Bus(Class<T> enumType,
        EventProcessor<T> eventProcessor,
        Map<T, List<Subscriber>> subscribers,
        Map<T, List<RequestSubscriber>> requestSubscribers,
        Map<T, List<ContentSubscriber>> contentSubscribers,
        Map<T, List<TransformerSubscriber>> transformerSubscribers,
        boolean logging) {

        this.enumType = enumType;
        this.eventProcessor = eventProcessor;
        this.subscribers = subscribers;
        this.requestSubscribers = requestSubscribers;
        this.contentSubscribers = contentSubscribers;
        this.transformerSubscribers = transformerSubscribers;
        this.logging = logging;
    }

    @SuppressWarnings("unused")
    public static <T extends Enum<T>> Bus<T> initializeBus(Class<T> enumType) {
        return initializeBus(enumType, false);
    }

    @SuppressWarnings("WeakerAccess")
    public static <T extends Enum<T>> Bus<T> initializeBus(Class<T> enumType, boolean enableLogging) {
        return initializeBus(enumType, enableLogging, 3);
    }

    @SuppressWarnings("WeakerAccess")
    public static <T extends Enum<T>> Bus<T> initializeBus(Class<T> enumType, boolean enableLogging, int threads) {
        Map<T, List<Subscriber>> subscribers = new EnumMap<>(enumType);
        Map<T, List<RequestSubscriber>> requestSubscribers = new EnumMap<>(enumType);
        Map<T, List<ContentSubscriber>> contentSubscribers = new EnumMap<>(enumType);
        Map<T, List<TransformerSubscriber>> transformerSubscribers = new EnumMap<>(enumType);
        EventProcessor<T> eventProcessor = new EventProcessor<>(
                subscribers,
                requestSubscribers,
                contentSubscribers,
                transformerSubscribers,
                Executors.newFixedThreadPool(threads),
                enumType);

        return new Bus<>(enumType,
                eventProcessor,
                subscribers,
                requestSubscribers,
                contentSubscribers,
                transformerSubscribers,
                enableLogging);
    }

    @SuppressWarnings({"unused", "unchecked"})
    public void unSubscribe(Object subscriber) {
        try {
            Subscriber toUnSubscribe = (Subscriber) subscriber;
            T messageType = (T) toUnSubscribe.getMessageType();
            unSubscribe(subscriber, messageType, subscribers);
            unSubscribe(subscriber, messageType, requestSubscribers);
            unSubscribe(subscriber, messageType, contentSubscribers);
            unSubscribe(subscriber, messageType, transformerSubscribers);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Provided subscriber cannot be unsubscribed. Most likely because it differ from object returned in Bus setReaction/Response methods");
        }
    }

    private void unSubscribe(Object subscriber, T msgType, Map<T, ? extends List> subscribers) {
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
    public void removeAllActionsFor(T topic) {
        subscribers.remove(topic);
        contentSubscribers.remove(topic);
        requestSubscribers.remove(topic);
        transformerSubscribers.remove(topic);
    }

    @SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
    public Object setReaction(T topic, Action action) {
        Subscriber subscriber = getSubscriber(topic, action);
        subscribers.computeIfAbsent(topic, key -> new ArrayList<>()).add(subscriber);
        return subscriber;
    }

    @SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
    public <V> Object setReaction(T topic, Consumer<V> consumer) {
        ContentSubscriber contentSubscriber = getContentSubscriber(topic, consumer);
        contentSubscribers.computeIfAbsent(topic, key -> new ArrayList<>()).add(contentSubscriber);
        return contentSubscriber;
    }

    @SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
    public Object setResponse(T topic, Action reaction) {
        RequestSubscriber requestSubscriber = getRequestSubscriber(topic, reaction);
        requestSubscribers.computeIfAbsent(topic, key -> new ArrayList<>()).add(requestSubscriber);
        return requestSubscriber;
    }

    @SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
    public <V> Object setResponse(T topic, Supplier<V> supplier) {
        ProducerSubscriber producerSubscriber = getRequestProducerSubscriber(topic, supplier);
        requestSubscribers.computeIfAbsent(topic, key -> new ArrayList<>()).add(producerSubscriber);
        return producerSubscriber;
    }

    @SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
    public <V, R> Object setResponse(T topic, Function<V, R> resolver) {
        TransformerSubscriber transformerSubscriber = getRequestContentSubscriber(topic, resolver);
        transformerSubscribers.computeIfAbsent(topic, key -> new ArrayList<>()).add(transformerSubscriber);
        return transformerSubscriber;
    }

    @SuppressWarnings("unused")
    public Object setFXReaction(T topic, Action action) {
        Subscriber subscriber = getGuiSubscriber(topic, action);
        subscribers.computeIfAbsent(topic, key -> new ArrayList<>()).add(subscriber);
        return subscriber;
    }

    @SuppressWarnings("unused")
    public <V> Object setFXReaction(T topic, Consumer<V> consumer) {
        ContentSubscriber subscriber = getGuiContentSubscriber(topic, consumer);
        contentSubscribers.computeIfAbsent(topic, key -> new ArrayList<>()).add(subscriber);
        return subscriber;
    }

    @SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
    public Object setFXResponse(T topic, Action reaction) {
        RequestSubscriber requestSubscriber = getGuiRequestSubscriber(topic, reaction);
        requestSubscribers.computeIfAbsent(topic, key -> new ArrayList<>()).add(requestSubscriber);
        return requestSubscriber;
    }

    @SuppressWarnings("unused")
    public <V, R> Object setFXResponse(T topic, Function<V, R> responseResolver) {
        TransformerSubscriber subscriber = getGuiRequestContentSubscriber(topic, responseResolver);
        transformerSubscribers.computeIfAbsent(topic, key -> new ArrayList<>()).add(subscriber);
        return subscriber;
    }

    @SuppressWarnings("unused")
    public <V> Object setFXResponse(T topic, Supplier<V> supplier) {
        ProducerSubscriber subscriber = getGuiRequestContentSubscriber(topic, supplier);
        requestSubscribers.computeIfAbsent(topic, key -> new ArrayList<>()).add(subscriber);
        return subscriber;
    }

    @SuppressWarnings("WeakerAccess")
    public void send(T communicate) {
        eventProcessor.processEvent(communicate);
    }

    @SuppressWarnings("unused")
    public <V> void send(T topic, V content) {
        eventProcessor.processContent(topic, content);
    }

    @SuppressWarnings("unused")
    public <V> void send(T topic, T responseTopic, V content) {
        eventProcessor.processRequestWithContent(topic, responseTopic, content, null);
    }

    @SuppressWarnings("unused")
    public <V> void send(T topic,
                         T responseTopic,
                         V content,
                         T errorTopic) {
        eventProcessor.processRequestWithContent(topic, responseTopic, content, errorTopic);
    }

    @SuppressWarnings("unused")
    public void sendMessage(T communicate, T responseTopic) {
        eventProcessor.processRequest(communicate, responseTopic, null);
    }

    @SuppressWarnings("unused")
    public void sendMessage(T communicate,
                            T responseTopic,
                            T errorTopic) {
        eventProcessor.processRequest(communicate, responseTopic, errorTopic);
    }

    private Subscriber getSubscriber(T topic, Action reaction) {
        CommunicateSubscriber subscriber = new CommunicateSubscriber();
        subscriber.setMessageType(topic);
        subscriber.setAction(reaction);
        return subscriber;
    }

    private <V> ContentSubscriber getContentSubscriber(T topic, Consumer<V> consumer) {
        BagSubscriber bagSubscriber = new BagSubscriber(topic);
        bagSubscriber.setConsumer(consumer);
        return bagSubscriber;
    }

    private RequestSubscriber getRequestSubscriber(T topic, Action reaction) {
        RequestMessageSubscriber subscriber = new RequestMessageSubscriber(this);
        subscriber.setAction(reaction);
        subscriber.setMessageType(topic);
        return subscriber;
    }

    private Subscriber getGuiSubscriber(T topic, Action action) {
        GuiCommunicateSubscriber subscriber = new GuiCommunicateSubscriber();
        subscriber.setMessageType(topic);
        subscriber.setAction(action);
        return subscriber;
    }

    private RequestSubscriber getGuiRequestSubscriber(T topic, Action action) {
        GuiRequestSubscriber subscriber = new GuiRequestSubscriber(this);
        subscriber.setMessageType(topic);
        subscriber.setAction(action);
        return subscriber;
    }

    private <V> ContentSubscriber getGuiContentSubscriber(T topic, Consumer<V> consumer) {
        GuiContentSubscriber subscriber = new GuiContentSubscriber(topic);
        subscriber.setConsumer(consumer);
        return subscriber;
    }

    private <V, R> TransformerSubscriber getGuiRequestContentSubscriber(
            T topic, Function<V, R> responseResolver) {
        GuiRequestBagSubscriber subscriber = new GuiRequestBagSubscriber(this, topic);
        subscriber.setResponseResolver(responseResolver);
        return subscriber;
    }

    private <V> ProducerSubscriber getGuiRequestContentSubscriber(
            T topic, Supplier<V> supplier) {
        GuiRequestProducerSubscriber subscriber = new GuiRequestProducerSubscriber(this);
        subscriber.setMessageType(topic);
        subscriber.setResponseProducer(supplier);
        return subscriber;
    }

    private <V> ProducerSubscriber getRequestProducerSubscriber(
            T topic, Supplier<V> supplier) {
        RequestProducerSubscriber subscriber = new RequestProducerSubscriber(this);
        subscriber.setMessageType(topic);
        subscriber.setResponseProducer(supplier);
        return subscriber;
    }

    private <V, R> TransformerSubscriber getRequestContentSubscriber(
            T topic, Function<V, R> responseResolver) {
        RequestBagSubscriber subscriber = new RequestBagSubscriber(this, topic);
        subscriber.setMessageType(topic);
        subscriber.setResponseResolver(responseResolver);
        return subscriber;
    }
}
