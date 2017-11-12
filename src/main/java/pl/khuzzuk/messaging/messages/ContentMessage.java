package pl.khuzzuk.messaging.messages;

public class ContentMessage<T> extends AbstractMessage implements BagMessage<T> {
    private String type;
    private T content;
    @Override
    public ContentMessage<T> setMessage(T content) {
        this.content = content;
        return this;
    }

    @Override
    public T getMessage() {
        return content;
    }

    @Override
    public BagMessage<T> setType(String type) {
        this.type = type;
        return this;
    }

    public String toString() {
        return "ContentMessage(type=" + this.type + ")";
    }

    public String getType() {
        return this.type;
    }
}
