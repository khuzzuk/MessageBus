package pl.khuzzuk.messaging.subscribers;

import javafx.application.Platform;

public class GuiContentSubscriber extends AbstractContentSubscriber {
    public GuiContentSubscriber(Enum<? extends Enum> msgType) {
        this.setMessageType(msgType);
    }

    @Override
    public <T> void receive(T content) {
        if (action != null) {
            Platform.runLater(() -> action.resolve());
        }
        Platform.runLater(() -> consumer.accept(content));
    }
}
