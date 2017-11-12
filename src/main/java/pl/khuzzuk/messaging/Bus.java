package pl.khuzzuk.messaging;

import org.apache.logging.log4j.Logger;
import pl.khuzzuk.messaging.messages.*;
import pl.khuzzuk.messaging.publisher.*;
import pl.khuzzuk.messaging.subscribers.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Bus {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger("MessageLogger");
    private final BlockingQueue<Message> channel;
    private final Map<String, List<Subscriber<? extends Message>>> subscribers;
    private static boolean logging;

    Bus(BlockingQueue<Message> channel, Map<String, List<Subscriber<? extends Message>>> subscribers) {
        this.channel = channel;
        this.subscribers = subscribers;
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
        BlockingQueue<Message> channel = new LinkedBlockingQueue<>();
        Map<String, List<Subscriber<? extends Message>>> subscribers = new HashMap<>();
        MessageWorker messageWorker = new MessageWorker(subscribers, channel, Executors.newFixedThreadPool(threads));
        messageWorker.startWorker(enableLogging);
        logging = enableLogging;
        return new Bus(channel, subscribers);
    }

    public void subscribe(Subscriber<?> subscriber, String messageType) {
        subscribers.computeIfAbsent(messageType, k -> new ArrayList<>());
        subscribers.get(messageType).add(subscriber);
    }

    public void unSubscribe(Subscriber<?> subscriber, String msgType) {
        subscribers.get(msgType).remove(subscriber);
    }

    @SuppressWarnings("unused")
    public void closeBus() {
        publish(new CommunicateMessage().setType("closeBus"));
    }

    @SuppressWarnings("unused")
    public void removeAllActionsFor(String topic) {
        subscribers.get(topic).forEach(Subscriber::unSubscribe);
    }

    @SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
    public Object setReaction(String topic, Action action) {
        return getSubscriber(topic, action);
    }

    @SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
    public <T> Object setReaction(String topic, Consumer<T> consumer) {
        return getContentSubscriber(topic, consumer);
    }

    @SuppressWarnings("unused")
    public Object setResponse(String topic, Action reaction) {
        return getRequestSubscriber(topic, reaction);
    }

    @SuppressWarnings("unused")
    public <T> Object setResponse(String topic, Supplier<T> supplier) {
        return getRequestProducerSubscriber(topic, supplier);
    }

    @SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
    public <V, R> Object setResponse(String topic, Function<V, R> resolver) {
        return getRequestContentSubscriber(topic, resolver);
    }

    @SuppressWarnings("unused")
    public Object setGuiReaction(String topic, Action action) {
        return getGuiSubscriber(topic, action);
    }

    @SuppressWarnings("unused")
    public <T> Object setGuiReaction(String topic, Consumer<T> consumer) {
        return getGuiContentSubscriber(topic, consumer);
    }

    @SuppressWarnings("unused")
    public <T, R> Object setGuiResponse(String topic, Function<T, R> responseResolver) {
        return getGuiRequestContentSubscriber(topic, responseResolver);
    }

    @SuppressWarnings("unused")
    public <T> Object setGuiResponse(String topic, Supplier<T> supplier) {
        return getGuiRequestContentSubscriber(topic, supplier);
    }

    @SuppressWarnings("WeakerAccess")
    public void send(String communicate) {
        publish(getCommunicate(communicate));
    }

    @SuppressWarnings("unused")
    public <T> void send(String topic, T content) {
        publish(getBagMessage(topic, content));
    }

    @SuppressWarnings("unused")
    public <T> void send(String topic, String responseTopic, T content) {
        publish(getBagRequest(topic, responseTopic, content));
    }

    @SuppressWarnings("unused")
    public <T> void send(String topic, String responseTopic, T content, String errorTopic) {
        publish(getBagRequest(topic, responseTopic, content, errorTopic));
    }

    @SuppressWarnings("unused")
    public void sendCommunicate(String communicate, String responseTopic) {
        publish(getRequest(communicate, responseTopic));
    }

    @SuppressWarnings("unused")
    public void sendCommunicate(String communicate, String responseTopic, String errorTopic) {
        publish(getRequest(communicate, responseTopic, errorTopic));
    }

    private Message getCommunicate(String topic) {
        return new CommunicateMessage().setType(topic);
    }

    @SuppressWarnings("unchecked")
    private <T> BagMessage<T> getBagMessage(String topic, T content) {
        return (BagMessage<T>) new ContentMessage<>().setType(topic).setMessage(content);
    }

    RequestMessage getRequest(String topic, String responseType) {
        return new CommunicateRequest().setType(topic).setResponseType(responseType);
    }

    RequestMessage getRequest(String topic, String responseType, String errorType) {
        return new CommunicateRequest().setType(topic).setResponseType(responseType).setErrorType(errorType);
    }

    <T> RequestBagMessage<T> getBagRequest(String topic, String responseType, T content) {
        return new RequestContentMessage<T>().setType(topic)
                .setResponseType(responseType).setMessage(content);
    }

    <T> RequestBagMessage<T> getBagRequest(String topic, String responseType, T content, String errorType) {
        return new RequestContentMessage<T>().setType(topic)
                .setResponseType(responseType).setMessage(content).setErrorType(errorType);
    }

    <T extends Message> Publisher<T> getPublisher() {
        CommunicatePublisher<T> publisher = new CommunicatePublisher<>();
        publisher.setBus(this);
        return publisher;
    }

    <T> BagPublisher<T> getBagPublisher() {
        ContentPublisher<T> publisher = new ContentPublisher<>();
        publisher.setBus(this);
        return publisher;
    }

    @SuppressWarnings("unused")
    private MultiContentPublisher getMultiContentPublisher() {
        MultiBagPublisher publisher = new MultiBagPublisher();
        publisher.setBus(this);
        return publisher;
    }

    Subscriber<Message> getSubscriber(String topic, Action reaction) {
        CommunicateSubscriber subscriber = new CommunicateSubscriber();
        subscriber.setBus(this);
        subscriber.setMessageType(topic);
        subscriber.setAction(reaction);
        subscriber.subscribe();
        return subscriber;
    }

    <T, M extends BagMessage<T>> ContentSubscriber<T, M> getContentSubscriber(
            String topic, Consumer<T> consumer) {
        BagSubscriber<T, M> bagSubscriber = new BagSubscriber<>(topic);
        bagSubscriber.setBus(this);
        bagSubscriber.setConsumer(consumer);
        bagSubscriber.subscribe();
        return bagSubscriber;
    }

    Subscriber<RequestMessage> getRequestSubscriber(String topic, Action reaction) {
        RequestCommunicateSubscriber subscriber = new RequestCommunicateSubscriber();
        subscriber.setBus(this);
        subscriber.setAction(reaction);
        subscriber.setMessageType(topic);
        subscriber.subscribe();
        return subscriber;
    }

    private Subscriber<Message> getGuiSubscriber(String topic, Action action) {
        GuiCommunicateSubscriber subscriber = new GuiCommunicateSubscriber();
        subscriber.setBus(this);
        subscriber.setMessageType(topic);
        subscriber.setAction(action);
        subscriber.subscribe();
        return subscriber;
    }

    private <T, M extends BagMessage<T>> ContentSubscriber<T, M> getGuiContentSubscriber(
            String topic, Consumer<T> consumer) {
        GuiContentSubscriber<T, M> subscriber = new GuiContentSubscriber<>(topic);
        subscriber.setBus(this);
        subscriber.setConsumer(consumer);
        subscriber.subscribe();
        return subscriber;
    }

    private <T, R> RequestContentSubscriber<T, R> getGuiRequestContentSubscriber(
            String topic, Function<T, R> responseResolver) {
        GuiRequestBagSubscriber<T, R> subscriber = new GuiRequestBagSubscriber<T, R>();
        subscriber.setBus(this);
        subscriber.setMessageType(topic);
        subscriber.setResponseResolver(responseResolver);
        subscriber.subscribe();
        return subscriber;
    }

    private <T> RequestProducerSubscriber<T> getGuiRequestContentSubscriber(
            String topic, Supplier<T> supplier) {
        GuiRequestCommunicateProducerSubscriber<T> subscriber = new GuiRequestCommunicateProducerSubscriber<>();
        subscriber.setBus(this);
        subscriber.setMessageType(topic);
        subscriber.setResponseProducer(supplier);
        subscriber.subscribe();
        return subscriber;
    }

    private <V> RequestProducerSubscriber<V> getRequestProducerSubscriber(
            String topic, Supplier<V> supplier) {
        RequestCommunicateProducerSubscriber<V> subscriber = new RequestCommunicateProducerSubscriber<>();
        subscriber.setBus(this);
        subscriber.setMessageType(topic);
        subscriber.setResponseProducer(supplier);
        subscriber.subscribe();
        return subscriber;
    }

    private <V, R> RequestContentSubscriber<V, R> getRequestContentSubscriber(
            String topic, Function<V, R> responseResolver) {
        RequestBagSubscriber<V, R> subscriber = new RequestBagSubscriber<>();
        subscriber.setBus(this);
        subscriber.setMessageType(topic);
        subscriber.setResponseResolver(responseResolver);
        subscriber.subscribe();
        return subscriber;
    }

    @SuppressWarnings("unused")
    private MultiSubscriber<Message> getMultiSubscriber() {
        MultiCommunicateSubscriber subscriber = new MultiCommunicateSubscriber();
        subscriber.setBus(this);
        return subscriber;
    }

    @SuppressWarnings("unused")
    private MultiSubscriber<RequestMessage> getMultiRequestSubscriber() {
        MultiRequestSubscriber subscriber = new MultiRequestSubscriber();
        subscriber.setBus(this);
        return subscriber;
    }

    @SuppressWarnings("unused")
    private MultiContentSubscriber getMultiContentSubscriber() {
        MultiBagSubscriber subscriber = new MultiBagSubscriber();
        subscriber.setBus(this);
        return subscriber;
    }

    @SuppressWarnings("unused")
    private MultiContentSubscriber getGuiMultiContentSubscriber() {
        GuiMultiBagSubscriber subscriber = new GuiMultiBagSubscriber();
        subscriber.setBus(this);
        return subscriber;
    }

    @SuppressWarnings("unused")
    private MultiRequestContentSubscriber getMultiRequestContentSubscriber() {
        MultiRequestBagSubscriber subscriber = new MultiRequestBagSubscriber();
        subscriber.setBus(this);
        return subscriber;
    }

    @SuppressWarnings("unused")
    private MultiRequestContentSubscriber getGuiMultiRequestContentSubscriber() {
        GuiMultiRequestBagSubscriber subscriber = new GuiMultiRequestBagSubscriber();
        subscriber.setBus(this);
        return subscriber;
    }

    public void publish(Message message) {
        try {
            if (logging) log.info("accepted message: " + message);
            channel.put(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
