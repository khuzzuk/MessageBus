package pl.khuzzuk.messaging;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

@Log4j2(topic = "MessageLogger")
public class Bus implements Publisher {
    private final BlockingQueue<Message> channel;
    private final MultiValuedMap<String, Subscriber<? extends Message>> subscribers;
    private final MessageWorker messageWorker;
    private MultiBagSubscriber bagSubscribers;
    private GuiMultiBagSubscriber guiSubscribers;

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
        bus.bagSubscribers = new MultiBagSubscriber();
        bus.bagSubscribers.setBus(bus);
        bus.guiSubscribers = new GuiMultiBagSubscriber();
        bus.guiSubscribers.setBus(bus);
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
        bagSubscribers.unSubscribe(topic);
    }

    public void setReaction(String topic, Reactor reactor) {
        bagSubscribers.subscribe(topic, reactor);
    }

    public void setReaction(String topic, Consumer consumer) {
        bagSubscribers.subscribe(topic, consumer);
    }

    public void setGuiReaction(String topic, Reactor reactor) {
        guiSubscribers.subscribe(topic, reactor);
    }

    public void setGuiReaction(String topic, Consumer consumer) {
        guiSubscribers.subscribe(topic, consumer);
    }

    public Message getCommunicate(String topic) {
        return new CommunicateMessage().setType(topic);
    }

    @SuppressWarnings("unchecked")
    public <T> BagMessage<T> getBagMessage(String topic, T content) {
        return (BagMessage<T>) new ContentMessage<>().setType(topic).setMessage(content);
    }

    public Publisher<Message> getPublisher() {
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

    public Subscriber<Message> getSubscriber(String topic) {
        CommunicateSubscriber subscriber = new CommunicateSubscriber();
        subscriber.setBus(this);
        subscriber.setMessageType(topic);
        subscriber.subscribe();
        return subscriber;
    }

    public <T> ContentSubscriber<T> getContentSubscriber(String topic) {
        BagSubscriber<T> bagSubscriber = new BagSubscriber<>(topic);
        bagSubscriber.setBus(this);
        bagSubscriber.subscribe();
        return bagSubscriber;
    }

    public <T> ContentSubscriber<T> getGuiContentSubscriber(String topic) {
        GuiContentSubscriber<T> subscriber = new GuiContentSubscriber<>(topic);
        subscriber.setBus(this);
        subscriber.subscribe();
        return subscriber;
    }

    public MultiSubscriber<Message> getMultiSubscriber() {
        MultiCommunicateSubscriber subscriber = new MultiCommunicateSubscriber();
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
