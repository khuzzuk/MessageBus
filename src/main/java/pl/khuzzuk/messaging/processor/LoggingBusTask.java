package pl.khuzzuk.messaging.processor;

import pl.khuzzuk.messaging.message.Message;
import pl.khuzzuk.messaging.subscriber.Subscriber;

public class LoggingBusTask<T extends Enum<T>> extends BusTask<T>
{
   LoggingBusTask(Message<T> message,
         Subscriber<T> subscriber,
         BusContext<T> busContext)
   {
      super(message, subscriber, busContext);
   }

   @Override
   public void run()
   {
      busContext.out.println(String.format("Receiving: %s%n\tfor: %s", message, subscriber));
      super.run();
      busContext.out.println(String.format("Finished: %s%n\tfor: %s", message, subscriber));
   }
}
