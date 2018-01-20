package pl.khuzzuk.messaging.subscribers;

public interface RequestSubscriber extends Subscriber {
    void receive(Enum<? extends Enum> responseTopic, Enum<? extends Enum> errorTopic);
}
