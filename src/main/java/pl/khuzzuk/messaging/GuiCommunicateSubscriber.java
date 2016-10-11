package pl.khuzzuk.messaging;

import javafx.application.Platform;

class GuiCommunicateSubscriber extends CommunicateSubscriber {
    @Override
    public void receive(Message message) {
        Platform.runLater(() -> super.receive(message));
    }
}
