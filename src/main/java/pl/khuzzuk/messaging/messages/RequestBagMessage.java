package pl.khuzzuk.messaging.messages;

public interface RequestBagMessage<T> extends BagMessage<T>, RequestMessage {
    @Override
    RequestBagMessage<T> setType(String type);

    @Override
    RequestBagMessage<T> setResponseType(String resoponseType);

    @Override
    RequestBagMessage<T> setMessage(T content);

    @Override
    RequestBagMessage<T> setErrorType(String errorType);
}
