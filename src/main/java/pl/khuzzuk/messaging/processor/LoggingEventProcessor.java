package pl.khuzzuk.messaging.processor;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import pl.khuzzuk.messaging.message.Message;
import pl.khuzzuk.messaging.subscriber.Subscriber;

public class LoggingEventProcessor<T extends Enum<T>> extends EventProcessor<T>
{
   public LoggingEventProcessor(
         Map<T, List<Subscriber<T>>> subscribers,
         ExecutorService pool, PrintStream out)
   {
      super(subscribers, pool, out);
   }

   @Override
   public void processEvent(Message<T> message)
   {
      busContext.out.println(String.format("Accepted: %s", message));
      super.processEvent(message);
   }

   @Override
   void submitTask(Message<T> message, Subscriber<T> subscriber)
   {
      pool.submit(new LoggingBusTask<>(message, subscriber, busContext));
   }
}
