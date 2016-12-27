package pl.khuzzuk.messaging;

import java.util.function.Function;

class RequestBagSubscriber<T, R> extends AbstractContentSubscriber<T, RequestBagMessage<T>> implements RequestContentSubscriber<T, R> {
    private Function<T, R> responseResolvers;

    @Override
    public void receive(RequestBagMessage<T> message) {
        if (responseResolvers != null) {
            getBus().publish(new ContentMessage<R>().setType(message.getResponseType()).setMessage(
                    responseResolvers.apply(message.getMessage())));
        } else {
            super.receive(message);
        }
    }

    @Override
    public RequestContentSubscriber<T, R> setResponseResolver(Function<T, R> responseResolver) {
        this.responseResolvers = responseResolver;
        return this;
    }
}
