package pl.khuzzuk.messaging.subscribers;

import javafx.application.Platform;

public class GuiCommunicateSubscriber extends CommunicateSubscriber {
    @Override
    public void receive() {
        Platform.runLater(() -> action.resolve());
    }
}
