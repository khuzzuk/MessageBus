package pl.khuzzuk.messaging;

class CommunicateMessage extends AbstractMessage {
    @Override
    public String toString() {
        return "Message=" + getType();
    }
}
