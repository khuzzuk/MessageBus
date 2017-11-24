package pl.khuzzuk.messaging.subscribers;

import java.util.function.Consumer;

abstract class AbstractContentSubscriber extends AbstractSubscriber implements ContentSubscriber {
    Consumer consumer;
    private Enum<? extends Enum> messageType;

    @Override
    public <T> void receive(T content) {
        consumer.accept(content);
    }

    public String toString() {
        return "ContentSubscriber(messageType=" + this.messageType + ")";
    }

    public Enum<? extends Enum> getMessageType() {
        return this.messageType;
    }

    public <T> void setConsumer(Consumer<T> consumer) {
        this.consumer = consumer;
    }

    public void setMessageType(Enum<? extends Enum> messageType) {
        this.messageType = messageType;
    }
}
