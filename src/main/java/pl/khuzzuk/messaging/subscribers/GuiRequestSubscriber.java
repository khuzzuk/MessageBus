package pl.khuzzuk.messaging.subscribers;

import javafx.application.Platform;
import pl.khuzzuk.messaging.messages.RequestMessage;

@SuppressWarnings("unused")
class GuiRequestSubscriber extends RequestCommunicateSubscriber {
    @SuppressWarnings("unused")
    @Override
    public void receive(RequestMessage message) {
        Platform.runLater(() -> super.receive(message));
    }
}
