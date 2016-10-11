package pl.khuzzuk.messaging;

import lombok.Getter;
import lombok.ToString;

@ToString(exclude = "content")
class ContentMessage<T> implements BagMessage<T> {
    @Getter
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
}
