package pl.khuzzuk.messaging.messages;

import pl.khuzzuk.messaging.Message;

public interface BagMessage<T> extends Message {
    BagMessage<T> setMessage(T content);

    BagMessage<T> setType(String type);

    T getMessage();
}
