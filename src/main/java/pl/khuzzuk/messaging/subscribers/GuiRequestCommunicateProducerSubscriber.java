package pl.khuzzuk.messaging.subscribers;

import javafx.application.Platform;
import pl.khuzzuk.messaging.messages.RequestMessage;

public class GuiRequestCommunicateProducerSubscriber<T> extends RequestCommunicateProducerSubscriber<T> {
    @Override
    public void receive(RequestMessage message) {
        Platform.runLater(() -> super.receive(message));
    }
}
