package pl.khuzzuk.messaging;

import lombok.Getter;
import lombok.ToString;

@ToString
abstract class AbstractMessage implements Message {
    @Getter
    private String type;

    @Override
    public Message setType(String type) {
        this.type = type;
        return this;
    }
}
