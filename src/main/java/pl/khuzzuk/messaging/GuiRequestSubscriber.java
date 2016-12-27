package pl.khuzzuk.messaging;

import javafx.application.Platform;

class GuiRequestSubscriber extends RequestCommunicateSubscriber {
    @Override
    public void receive(RequestMessage message) {
        Platform.runLater(() -> super.receive(message));
    }
}
