package pl.khuzzuk.messaging;

import javafx.application.Platform;

class GuiContentSubscriber<T, M extends BagMessage<T>> extends AbstractContentSubscriber<T, M> {
    public GuiContentSubscriber(String msgType) {
        this.setMessageType(msgType);
    }

    @Override
    public void receive(T content) {
        Platform.runLater(() -> consumer.accept(content));
    }
}
