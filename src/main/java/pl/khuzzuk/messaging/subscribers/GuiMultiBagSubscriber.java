package pl.khuzzuk.messaging.subscribers;

import javafx.application.Platform;
import pl.khuzzuk.messaging.messages.BagMessage;

public class GuiMultiBagSubscriber extends AbstractMultiContentSubscriber<BagMessage> {
    @Override
    public void receive(BagMessage message) {
        Platform.runLater(() -> super.receive(message));
    }

    @Override
    public void receive(Object content) {
        Platform.runLater(() -> super.receive(content));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
