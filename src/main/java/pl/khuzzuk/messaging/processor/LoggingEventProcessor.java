package pl.khuzzuk.messaging.processor;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;

import pl.khuzzuk.messaging.message.Message;
import pl.khuzzuk.messaging.subscriber.Subscriber;

public class LoggingEventProcessor<T extends Enum<T>> extends EventProcessor<T>
{
   public LoggingEventProcessor(
         Map<T, List<Subscriber<T>>> subscribers,
         ExecutorService pool, PrintStream out, Queue<Message<T>> messagesCache, Queue<? extends BusTask<T>> tasksCache)
   {
      super(subscribers, pool, out, messagesCache, tasksCache);
   }

   @Override
   public void processEvent(Message<T> message)
   {
      busContext.out.println(String.format("Accepted: %s", message));
      super.processEvent(message);
   }

   @Override
   void submitTask(Message<T> message, Subscriber<T> subscriber) {
      BusTask<T> task = tasksCache.poll();
      if (task == null) task = new LoggingBusTask<>(busContext);
      task.setMessage(message);
      task.setSubscriber(subscriber);
      pool.submit(task);
   }
}
