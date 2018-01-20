package pl.khuzzuk.messaging.subscribers;

import javafx.application.Platform;
import pl.khuzzuk.messaging.Bus;

public class GuiRequestBagSubscriber extends RequestBagSubscriber {
    public GuiRequestBagSubscriber(Bus bus, Enum<? extends Enum<?>> msgType) {
        super(bus, msgType);
    }

    @Override
    public <T> void receive(T content, Enum<? extends Enum<?>> responseTopic, Enum<? extends Enum<?>> errorTopic) {
        Platform.runLater(() -> super.receive(content, responseTopic, errorTopic));
    }
}
