package pl.khuzzuk.messaging.messages;

import pl.khuzzuk.messaging.Message;

abstract class AbstractMessage implements Message {
    private String type;

    String errorType;

    @Override
    public Message setType(String type) {
        this.type = type;
        return this;
    }

    public String toString() {
        return "AbstractMessage(type=" + this.type + ")";
    }

    public String getType() {
        return this.type;
    }

    @Override
    public String getErrorType() {
        return errorType;
    }

    @Override
    public Message setErrorType(String errorType) {
        this.errorType = errorType;
        return this;
    }
}
