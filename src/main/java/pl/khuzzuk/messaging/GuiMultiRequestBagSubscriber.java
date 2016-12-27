package pl.khuzzuk.messaging;

import javafx.application.Platform;

class GuiMultiRequestBagSubscriber extends MultiRequestBagSubscriber {
    @Override
    public void receive(RequestBagMessage message) {
        Platform.runLater(() -> super.receive(message));
    }

    @Override
    public void receive(Object content) {
        Platform.runLater(() -> super.receive(content));
    }
}
