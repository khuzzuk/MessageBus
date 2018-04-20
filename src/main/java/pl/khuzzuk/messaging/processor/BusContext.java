package pl.khuzzuk.messaging.processor;

import java.io.PrintStream;
import java.util.Queue;

import pl.khuzzuk.messaging.message.Message;

public class BusContext<T extends Enum<T>>
{
   PrintStream out;
   final EventProcessor<T> eventProcessor;
   private final Queue<Message<T>> messagesCache;
   private final Queue tasksCache;

   public BusContext(PrintStream out, EventProcessor<T> eventProcessor,
         Queue<Message<T>> messagesCache, Queue<? extends BusTask<T>> tasksCache) {
      this.out = out;
      this.eventProcessor = eventProcessor;
      this.messagesCache = messagesCache;
      this.tasksCache = tasksCache;
   }

   public void setOut(PrintStream out)
   {
      this.out = out;
   }

   void returnMessageToCache(Message<T> message) {
      message.clear();
      messagesCache.offer(message);
   }

   @SuppressWarnings("unchecked")
   void returnTaskToCache(BusTask<T> task) {
      tasksCache.offer(task);
   }
}
