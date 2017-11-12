package pl.khuzzuk.messaging.subscribers;

import javafx.application.Platform;
import pl.khuzzuk.messaging.Message;

class GuiMultiRequestSubscriber extends GuiMultiSubscriber {
    @Override
    public void receive(Message message) {
        Platform.runLater(() -> super.receive(message));
    }
}
