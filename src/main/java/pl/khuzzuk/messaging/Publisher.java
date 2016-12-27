package pl.khuzzuk.messaging;

interface Publisher<T extends Message> {
    void publish(T message);
}
