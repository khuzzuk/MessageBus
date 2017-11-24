package pl.khuzzuk.messaging.subscribers;

import javafx.application.Platform;
import pl.khuzzuk.messaging.Bus;

//TODO make EventProcessor decide if task should be submitted to JavaFX thread or pool thread. At the moment it will end up travelling from pool to JavaFX.
@SuppressWarnings("unused")
class GuiRequestSubscriber extends RequestCommunicateSubscriber {
    public GuiRequestSubscriber(Bus bus) {
        super(bus);
    }

    @Override
    public void receive(Enum<? extends Enum> responseTopic) {
        Platform.runLater(() -> super.receive(responseTopic));
    }
}
