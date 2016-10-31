package pl.khuzzuk.messaging;

public interface RequestMessage extends Message {
    RequestMessage setResponseType(String resoponseType);

    String getResponseType();

    @Override
    RequestMessage setType(String type);
}
