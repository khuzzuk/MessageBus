package pl.khuzzuk.messaging;

import lombok.Getter;

public class CommunicateRequest extends AbstractMessage implements RequestMessage {
    @Getter
    private String responseType;

    @Override
    public String toString() {
        return "CommunicateRequest=" + getType() + "responseType='" + responseType;
    }

    @Override
    public RequestMessage setType(String type) {
        super.setType(type);
        return this;
    }

    @Override
    public RequestMessage setResponseType(String resoponseType) {
        this.responseType = resoponseType;
        return this;
    }
}
