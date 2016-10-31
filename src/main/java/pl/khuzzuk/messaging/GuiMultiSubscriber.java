package pl.khuzzuk.messaging;

import javafx.application.Platform;

class GuiMultiSubscriber extends AbstractMultiSubscriber<Message> {
    @Override
    public void receive(Message message) {
        Platform.runLater(() -> super.receive(message));
    }
}
