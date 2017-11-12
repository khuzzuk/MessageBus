package pl.khuzzuk.messaging.subscribers;

import javafx.application.Platform;
import pl.khuzzuk.messaging.messages.RequestBagMessage;

public class GuiRequestBagSubscriber<T, R> extends RequestBagSubscriber<T, R> {
    @Override
    public void receive(RequestBagMessage<T> message) {
        Platform.runLater(() -> super.receive(message));
    }

    @Override
    public void receive(T content) {
        Platform.runLater(() -> super.receive(content));
    }
}
