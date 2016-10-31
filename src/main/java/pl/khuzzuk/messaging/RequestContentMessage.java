package pl.khuzzuk.messaging;

import lombok.Getter;

public class RequestContentMessage<T> extends ContentMessage<T> implements RequestBagMessage<T> {
    @Getter
    private String responseType;

    @Override
    public RequestBagMessage<T> setType(String type) {
        super.setType(type);
        return this;
    }

    @Override
    public RequestBagMessage<T> setResponseType(String resoponseType) {
        this.responseType = resoponseType;
        return this;
    }

    @Override
    public RequestContentMessage<T> setMessage(T content) {
        super.setMessage(content);
        return this;
    }
}
