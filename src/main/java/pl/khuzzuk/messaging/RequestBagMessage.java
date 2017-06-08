package pl.khuzzuk.messaging;

interface RequestBagMessage<T> extends BagMessage<T>, RequestMessage {
    @Override
    RequestBagMessage<T> setType(String type);

    @Override
    RequestBagMessage<T> setResponseType(String resoponseType);

    @Override
    RequestBagMessage<T> setMessage(T content);
}