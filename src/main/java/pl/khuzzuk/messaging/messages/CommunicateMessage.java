package pl.khuzzuk.messaging.messages;

public class CommunicateMessage extends AbstractMessage {
    @Override
    @SuppressWarnings("unused")
    public String toString() {
        return "Message=" + getType();
    }
}
