package pl.khuzzuk.messaging;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Function;

@Log4j2(topic = "MessageLogger")
public class Bus implements Publisher {
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
        Bus bus = new Bus(channel, subscribers, messageWorker);
        return bus;
    }

    public void subscribe(Subscriber<? extends Message> subscriber, String messageType) {
        subscribers.put(messageType, subscriber);
    }

    public void unSubscribe(Subscriber<? extends Message> subscriber, String msgType) {
        subscribers.removeMapping(msgType, subscriber);
    }

    public void closeBus() {
        messageWorker.closeScheduler();
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

    public Message getCommunicate(String topic) {
        return new CommunicateMessage().setType(topic);
    }

    @SuppressWarnings("unchecked")
    public <T> BagMessage<T> getBagMessage(String topic, T content) {
        return (BagMessage<T>) new ContentMessage<>().setType(topic).setMessage(content);
    }

    public <T> RequestBagMessage<T> getBagRequest(String topic, String responseType, T content) {
        return new RequestContentMessage<T>().setType(topic)
                .setResponseType(responseType).setMessage(content);
    }

    public <T extends Message> Publisher<T> getPublisher() {
        CommunicatePublisher publisher = new CommunicatePublisher();
        publisher.setBus(this);
        return publisher;
    }

    public <T> BagPublisher<T> getBagPublisher() {
        ContentPublisher<T> publisher = new ContentPublisher<>();
        publisher.setBus(this);
        return publisher;
    }

    public MultiContentPublisher getMultiContentPublisher() {
        MultiBagPublisher publisher = new MultiBagPublisher();
        publisher.setBus(this);
        return publisher;
    }

    public Subscriber<Message> getSubscriber(String topic, Reactor reaction) {
        CommunicateSubscriber subscriber = new CommunicateSubscriber();
        subscriber.setBus(this);
        subscriber.setMessageType(topic);
        subscriber.setReactor(reaction);
        subscriber.subscribe();
        return subscriber;
    }

    public <T, M extends BagMessage<T>> ContentSubscriber<T, M> getContentSubscriber(
            String topic, Consumer<T> consumer) {
        BagSubscriber<T, M> bagSubscriber = new BagSubscriber<>(topic);
        bagSubscriber.setBus(this);
        bagSubscriber.setConsumer(consumer);
        bagSubscriber.subscribe();
        return bagSubscriber;
    }

    public Subscriber<RequestMessage> getRequestSubscriber(String topic, Reactor reaction) {
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

    public <T, M extends BagMessage<T>> ContentSubscriber<T, M> getGuiContentSubscriber(
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

    public <V, R> RequestContentSubscriber<V, R> getRequestContentSubscriber(
            String topic, Function<V, R> responseResolver) {
        RequestBagSubscriber<V, R> subscriber = new RequestBagSubscriber<>();
        subscriber.setBus(this);
        subscriber.setMessageType(topic);
        subscriber.setResponseResolver(responseResolver);
        subscriber.subscribe();
        return subscriber;
    }

    public MultiSubscriber<Message> getMultiSubscriber() {
        MultiCommunicateSubscriber subscriber = new MultiCommunicateSubscriber();
        subscriber.setBus(this);
        return subscriber;
    }

    public MultiSubscriber<RequestMessage> getMultiRequestSubscriber() {
        MultiRequestSubscriber subscriber = new MultiRequestSubscriber();
        subscriber.setBus(this);
        return subscriber;
    }

    public MultiContentSubscriber getMultiContentSubscriber() {
        MultiBagSubscriber subscriber = new MultiBagSubscriber();
        subscriber.setBus(this);
        return subscriber;
    }

    public MultiContentSubscriber getGuiMultiContentSubscriber() {
        GuiMultiBagSubscriber subscriber = new GuiMultiBagSubscriber();
        subscriber.setBus(this);
        return subscriber;
    }

    public MultiRequestContentSubscriber getMultiRequestContentSubscriber() {
        MultiRequestBagSubscriber subscriber = new MultiRequestBagSubscriber();
        subscriber.setBus(this);
        return subscriber;
    }

    public MultiRequestContentSubscriber getGuiMultiRequestContentSubscriber() {
        GuiMultiRequestBagSubscriber subscriber = new GuiMultiRequestBagSubscriber();
        subscriber.setBus(this);
        return subscriber;
    }

    public RequestMessage getRequest(String topic, String responseType) {
        return new CommunicateRequest().setType(topic).setResponseType(responseType);
    }

    @Override
    public void publish(Message message) {
        try {
            log.info("accepted message: " + message);
            channel.put(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
