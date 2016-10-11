package pl.khuzzuk.messaging;

import javafx.application.Platform;

class GuiMultiBagSubscriber extends AbstractMultiContentSubscriber {
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
