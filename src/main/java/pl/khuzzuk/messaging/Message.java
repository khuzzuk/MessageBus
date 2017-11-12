package pl.khuzzuk.messaging;

@SuppressWarnings("unused")
public interface Message {
    Message setType(String type);

    String getType();

    String getErrorType();

    Message setErrorType(String errorType);
}
