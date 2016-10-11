package pl.khuzzuk.messaging;

import javafx.application.Platform;

class GuiContentSubscriber<T> extends AbstractContentSubscriber<T> {
    public GuiContentSubscriber(String msgType) {
        this.setMessageType(msgType);
    }

    @Override
    public void receive(T content) {
        Platform.runLater(() -> consumer.accept(content));
    }
}
