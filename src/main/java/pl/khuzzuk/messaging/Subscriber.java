package pl.khuzzuk.messaging;

public interface Subscriber<T extends Message> {
    void receive(T message);
    void subscribe();
    void setMessageType(String messageType);
    String getMessageType();
    void setReactor(Reactor reactor);
    void unSubscribe();
}
