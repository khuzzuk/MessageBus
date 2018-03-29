package pl.khuzzuk.messaging.subscribers;

import java.util.function.Function;

import pl.khuzzuk.messaging.Bus;

public class RequestBagSubscriber extends AbstractContentSubscriber implements TransformerSubscriber {
    private Function responseResolver;
    Bus bus;

    public RequestBagSubscriber(Bus bus, Enum<? extends Enum<?>> msgType) {
        this.bus = bus;
        this.setMessageType(msgType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void receive(T content, Enum<? extends Enum<?>> responseTopic) {
        Object responseContent = responseResolver.apply(content);
        bus.message(responseTopic).withContent(responseContent).send();
    }

    @Override
    public <T, R> TransformerSubscriber setResponseResolver(Function<T, R> responseResolver) {
        this.responseResolver = responseResolver;
        return this;
    }
}
