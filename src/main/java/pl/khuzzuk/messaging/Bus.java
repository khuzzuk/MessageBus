package pl.khuzzuk.messaging;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Log4j2(topic = "MessageLogger")
public class Bus {
    private final BlockingQueue<Message> channel;
    private final MultiValuedMap<String, Subscriber<? extends Message>> subscribers;
    private final MessageWorker messageWorker;

    private Bus(BlockingQueue<Message> channel, MultiValuedMap<String, Subscriber<? extends Message>> subscribers, MessageWorker messageWorker) {
        this.channel = channel;
        this.subscribers = subscribers;
        this.messageWorker = messageWorker;
    }

    public static Bus initializeBus() {
        BlockingQueue<Message> channel = new LinkedBlockingQueue<>();
        HashSetValuedHashMap<String, Subscriber<? extends Message>> subscribers = new HashSetValuedHashMap<>();
        MessageWorker messageWorker = new MessageWorker(subscribers, channel, Executors.newFixedThreadPool(3));
        messageWorker.startWorker();
        return new Bus(channel, subscribers, messageWorker);
    }

    void subscribe(Subscriber<? extends Message> subscriber, String messageType) {
        subscribers.put(messageType, subscriber);
    }

    void unSubscribe(Subscriber<? extends Message> subscriber, String msgType) {
        subscribers.removeMapping(msgType, subscriber);
    }

    public void closeBus() {
        publish(new CommunicateMessage().setType("closeBus"));
    }

    public void removeAllActionsFor(String topic) {
        subscribers.get(topic).forEach(Subscriber::unSubscribe);
    }

    public void setReaction(String topic, Reactor reactor) {
        getSubscriber(topic, reactor);
    }

    public <T> void setReaction(String topic, Consumer<T> consumer) {
        getContentSubscriber(topic, consumer);
    }

    public void setResponse(String topic, Reactor reaction) {
        getRequestSubscriber(topic, reaction);
    }

    public <T> void setResponse(String topic, Supplier<T> supplier) {
        getRequestProducerSubscriber(topic, supplier);
    }

    public <V, R> void setResponseResolver(String topic, Function<V, R> resolver) {
        getRequestContentSubscriber(topic, resolver);
    }

    public void setGuiReaction(String topic, Reactor reactor) {
        getGuiSubscriber(topic, reactor);
    }

    public void setGuiReaction(String topic, Consumer consumer) {
        getGuiContentSubscriber(topic, consumer);
    }

    public void setGuiResponse(String topic, Function responseResolver) {
        getGuiRequestContentSubscriber(topic, responseResolver);
    }

    public void send(String communicate) {
        publish(getCommunicate(communicate));
    }

    public <T> void send(String topic, T content) {
        publish(getBagMessage(topic, content));
    }

    public <T> void send(String topic, String responseTopic, T content) {
        publish(getBagRequest(topic, responseTopic, content));
    }

    public void send(String communicate, String responseTopic) {
        publish(getRequest(communicate, responseTopic));
    }

    private Message getCommunicate(String topic) {
        return new CommunicateMessage().setType(topic);
    }

    @SuppressWarnings("unchecked")
    private <T> BagMessage<T> getBagMessage(String topic, T content) {
        return (BagMessage<T>) new ContentMessage<>().setType(topic).setMessage(content);
    }

    private RequestMessage getRequest(String topic, String responseType) {
        return new CommunicateRequest().setType(topic).setResponseType(responseType);
    }

    private <T> RequestBagMessage<T> getBagRequest(String topic, String responseType, T content) {
        return new RequestContentMessage<T>().setType(topic)
                .setResponseType(responseType).setMessage(content);
    }

    private <T extends Message> Publisher<T> getPublisher() {
        CommunicatePublisher publisher = new CommunicatePublisher();
        publisher.setBus(this);
        return publisher;
    }

    private <T> BagPublisher<T> getBagPublisher() {
        ContentPublisher<T> publisher = new ContentPublisher<>();
        publisher.setBus(this);
        return publisher;
    }

    private MultiContentPublisher getMultiContentPublisher() {
        MultiBagPublisher publisher = new MultiBagPublisher();
        publisher.setBus(this);
        return publisher;
    }

    private Subscriber<Message> getSubscriber(String topic, Reactor reaction) {
        CommunicateSubscriber subscriber = new CommunicateSubscriber();
        subscriber.setBus(this);
        subscriber.setMessageType(topic);
        subscriber.setReactor(reaction);
        subscriber.subscribe();
        return subscriber;
    }

    private <T, M extends BagMessage<T>> ContentSubscriber<T, M> getContentSubscriber(
            String topic, Consumer<T> consumer) {
        BagSubscriber<T, M> bagSubscriber = new BagSubscriber<>(topic);
        bagSubscriber.setBus(this);
        bagSubscriber.setConsumer(consumer);
        bagSubscriber.subscribe();
        return bagSubscriber;
    }

    private Subscriber<RequestMessage> getRequestSubscriber(String topic, Reactor reaction) {
        RequestCommunicateSubscriber subscriber = new RequestCommunicateSubscriber();
        subscriber.setBus(this);
        subscriber.setReactor(reaction);
        subscriber.setMessageType(topic);
        subscriber.subscribe();
        return subscriber;
    }

    private Subscriber<Message> getGuiSubscriber(String topic, Reactor reactor) {
        GuiCommunicateSubscriber subscriber = new GuiCommunicateSubscriber();
        subscriber.setMessageType(topic);
        subscriber.setReactor(reactor);
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

    private MultiSubscriber<Message> getMultiSubscriber() {
        MultiCommunicateSubscriber subscriber = new MultiCommunicateSubscriber();
        subscriber.setBus(this);
        return subscriber;
    }

    private MultiSubscriber<RequestMessage> getMultiRequestSubscriber() {
        MultiRequestSubscriber subscriber = new MultiRequestSubscriber();
        subscriber.setBus(this);
        return subscriber;
    }

    private MultiContentSubscriber getMultiContentSubscriber() {
        MultiBagSubscriber subscriber = new MultiBagSubscriber();
        subscriber.setBus(this);
        return subscriber;
    }

    private MultiContentSubscriber getGuiMultiContentSubscriber() {
        GuiMultiBagSubscriber subscriber = new GuiMultiBagSubscriber();
        subscriber.setBus(this);
        return subscriber;
    }

    private MultiRequestContentSubscriber getMultiRequestContentSubscriber() {
        MultiRequestBagSubscriber subscriber = new MultiRequestBagSubscriber();
        subscriber.setBus(this);
        return subscriber;
    }

    private MultiRequestContentSubscriber getGuiMultiRequestContentSubscriber() {
        GuiMultiRequestBagSubscriber subscriber = new GuiMultiRequestBagSubscriber();
        subscriber.setBus(this);
        return subscriber;
    }

    void publish(Message message) {
        try {
            log.info("accepted message: " + message);
            channel.put(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
