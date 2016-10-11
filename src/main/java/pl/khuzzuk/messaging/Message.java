package pl.khuzzuk.messaging;

public interface Message {
    Message setType(String type);
    String getType();
}
