package pl.khuzzuk.messaging;

import javafx.application.Platform;

class GuiRequestBagSubscriber<T, R> extends RequestBagSubscriber<T, R> {
    @Override
    public void receive(RequestBagMessage<T> message) {
        Platform.runLater(() -> super.receive(message));
    }

    @Override
    public void receive(T content) {
        Platform.runLater(() -> super.receive(content));
    }
}
