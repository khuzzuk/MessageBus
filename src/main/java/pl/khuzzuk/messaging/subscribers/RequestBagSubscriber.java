package pl.khuzzuk.messaging.subscribers;

import pl.khuzzuk.messaging.Bus;

import java.util.function.Function;

public class RequestBagSubscriber extends AbstractContentSubscriber implements TransformerSubscriber {
    private Function responseResolver;
    Bus bus;

    public RequestBagSubscriber(Bus bus, Enum<? extends Enum<?>> msgType) {
        this.bus = bus;
        this.setMessageType(msgType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void receive(T content, Enum<? extends Enum<?>> responseTopic, Enum<? extends Enum<?>> errorTopic) {
        try {
            Object responseContent = responseResolver.apply(content);
            bus.send(responseTopic, responseContent);
        } catch (Throwable t) {
            t.printStackTrace();
            if (errorTopic != null) {
                bus.send(errorTopic);
            }
        }
    }

    @Override
    public <T, R> TransformerSubscriber setResponseResolver(Function<T, R> responseResolver) {
        this.responseResolver = responseResolver;
        return this;
    }
}
