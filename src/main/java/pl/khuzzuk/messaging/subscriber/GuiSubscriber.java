package pl.khuzzuk.messaging.subscriber;

import javafx.application.Platform;
import pl.khuzzuk.messaging.Bus;
import pl.khuzzuk.messaging.message.Message;

public class GuiSubscriber<T extends Enum<T>> extends Subscriber<T>
{
   public GuiSubscriber(Bus<T> bus)
   {
      super(bus);
   }

   @Override
   public void receive(Message<T> message)
   {
      Platform.runLater(() -> super.receive(message));
   }
}
