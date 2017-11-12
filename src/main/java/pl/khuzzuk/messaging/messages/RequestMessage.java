package pl.khuzzuk.messaging.messages;

import pl.khuzzuk.messaging.Message;

public interface RequestMessage extends Message {
    RequestMessage setResponseType(String resoponseType);

    String getResponseType();

    @Override
    RequestMessage setType(String type);

    @Override
    RequestMessage setErrorType(String errorType);
}
