package pl.khuzzuk.messaging.messages;

public class CommunicateRequest extends AbstractMessage implements RequestMessage {
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

    public String getResponseType() {
        return this.responseType;
    }

    @Override
    public CommunicateRequest setErrorType(String errorType) {
        this.errorType = errorType;
        return this;
    }
}
