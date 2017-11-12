package pl.khuzzuk.messaging.publisher;

import pl.khuzzuk.messaging.Message;

public interface Publisher<T extends Message> {
    void publish(T message);
}
