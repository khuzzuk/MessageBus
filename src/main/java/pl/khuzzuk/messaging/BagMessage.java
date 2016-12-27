package pl.khuzzuk.messaging;

interface BagMessage<T> extends Message {
    BagMessage<T> setMessage(T content);

    BagMessage<T> setType(String type);

    T getMessage();
}
