package pl.khuzzuk.messaging.subscribers;

import javafx.application.Platform;

public class GuiSimpleSubscriber extends SimpleSubscriber
{
    @Override
    public void receive() {
        Platform.runLater(() -> action.resolve());
    }
}
