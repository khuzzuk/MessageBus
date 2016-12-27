package pl.khuzzuk.messaging;

import javafx.application.Platform;

class GuiMultiRequestSubscriber extends GuiMultiSubscriber {
    @Override
    public void receive(Message message) {
        Platform.runLater(() -> super.receive(message));
    }
}
