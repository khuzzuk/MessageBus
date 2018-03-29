package pl.khuzzuk.messaging.subscribers;

import javafx.application.Platform;
import pl.khuzzuk.messaging.Bus;

public class GuiRequestProducerSubscriber extends RequestProducerSubscriber {
    private Bus bus;

    public GuiRequestProducerSubscriber(Bus bus) {
        super(bus);
    }

    @Override
    public void receive(Enum<? extends Enum> responseTopic) {
        Platform.runLater(() -> super.receive(responseTopic));
    }
}
