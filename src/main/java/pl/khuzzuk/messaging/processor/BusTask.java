package pl.khuzzuk.messaging.processor;

import pl.khuzzuk.messaging.message.Message;
import pl.khuzzuk.messaging.subscriber.Subscriber;

public class BusTask<T extends Enum<T>> implements Runnable
{
   final BusContext<T> busContext;
   Message<T> message;
   Subscriber<T> subscriber;

   public BusTask(BusContext<T> busContext) {
      this.busContext = busContext;
   }

   public void setMessage(Message<T> message) {
      this.message = message;
   }

   public void setSubscriber(Subscriber<T> subscriber) {
      this.subscriber = subscriber;
   }

   @Override
   public void run()
   {
      try
      {
         subscriber.receive(message);
      }
      catch (Exception t)
      {
         busContext.out.println(String.format("Error during task execution for topic: %s", message.getTopic()));
         t.printStackTrace(busContext.out);
         if (message.getOnError() != null) message.getOnError().resolve();
      }
      busContext.returnMessageToCache(message);
      message = null;
      subscriber = null;
      busContext.returnTaskToCache(this);
   }
}
