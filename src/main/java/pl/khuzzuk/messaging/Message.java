package pl.khuzzuk.messaging;

interface Message {
    Message setType(String type);
    String getType();
}
