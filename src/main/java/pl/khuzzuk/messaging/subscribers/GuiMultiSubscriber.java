package pl.khuzzuk.messaging.subscribers;

import javafx.application.Platform;
import pl.khuzzuk.messaging.Message;

@SuppressWarnings("unused")
class GuiMultiSubscriber extends AbstractMultiSubscriber<Message> {
    @Override
    public void receive(Message message) {
        Platform.runLater(() -> super.receive(message));
    }
}
