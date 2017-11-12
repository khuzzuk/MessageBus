package pl.khuzzuk.messaging.subscribers;

import javafx.application.Platform;
import pl.khuzzuk.messaging.messages.RequestBagMessage;

public class GuiMultiRequestBagSubscriber extends MultiRequestBagSubscriber {
    @Override
    public void receive(RequestBagMessage message) {
        Platform.runLater(() -> super.receive(message));
    }

    @Override
    public void receive(Object content) {
        Platform.runLater(() -> super.receive(content));
    }
}
