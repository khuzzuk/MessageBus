package pl.khuzzuk.messaging;

import javafx.application.Platform;

class GuiRequestCommunicateProducerSubscriber<T> extends RequestCommunicateProducerSubscriber<T> {
    @Override
    public void receive(RequestMessage message) {
        Platform.runLater(() -> super.receive(message));
    }
}
