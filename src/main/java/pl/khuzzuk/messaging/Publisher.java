package pl.khuzzuk.messaging;

public interface Publisher<T extends Message> {
    void publish(T message);
}
