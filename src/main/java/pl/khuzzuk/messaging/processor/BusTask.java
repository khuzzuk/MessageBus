package pl.khuzzuk.messaging.processor;

import pl.khuzzuk.messaging.message.Message;
import pl.khuzzuk.messaging.subscriber.Subscriber;

class BusTask<T extends Enum<T>> implements Runnable
{
   Message<T> message;
   Subscriber<T> subscriber;
   BusContext<T> busContext;

   BusTask(Message<T> message, Subscriber<T> subscriber, BusContext<T> busContext) {
      this.message = message;
      this.subscriber = subscriber;
      this.busContext = busContext;
   }

   @Override
   public void run()
   {
      try
      {
         subscriber.receive(message);

         if (message.getImmediateResponse() != null)
         {
            message.getImmediateResponse().resolve();
         }
      }
      catch (Exception t)
      {
         busContext.out.println(String.format("Error during task execution for topic: %s", message.getTopic()));
         t.printStackTrace(busContext.out);
         if (message.getOnError() != null) message.getOnError().resolve();
      }
   }
}
