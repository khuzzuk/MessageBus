package pl.khuzzuk.messaging.processor;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import pl.khuzzuk.messaging.Cancellable;
import pl.khuzzuk.messaging.message.Message;
import pl.khuzzuk.messaging.subscriber.Subscriber;

public class EventProcessor<T extends Enum<T>> {
    final Map<T, List<Subscriber<T>>> subscribers;
    final ExecutorService pool;
    final BusContext<T> busContext;
    final Queue<? extends BusTask<T>> tasksCache;

   public EventProcessor(Map<T, List<Subscriber<T>>> subscribers,
         ExecutorService pool,
         PrintStream out,
         Queue<Message<T>> messagesCache,
         Queue<? extends BusTask<T>> tasksCache)
   {
      this.subscribers = subscribers;
      this.pool = pool;
      this.tasksCache = tasksCache;
      busContext = new BusContext<>(out, this, messagesCache, tasksCache);
   }

   public void close() {
      busContext.out.println("Closing bus");
        try {
            pool.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        pool.shutdownNow();
    }

    public void processEvent(Message<T> message) {
        List<Subscriber<T>> currentSubscribers = subscribers.get(message.getTopic());
        if (currentSubscribers == null) {
            busContext.out.println(String.format("No subscribers for topic: %s", message.getTopic()));
            return;
        }

       for (Subscriber<T> sub : currentSubscribers)
       {
          submitTask(message, sub);
       }
    }

    void submitTask(Message<T> message, Subscriber<T> subscriber)
    {
       BusTask<T> task = tasksCache.poll();
       if (task == null) task = new BusTask<>(busContext);
       task.setMessage(message);
       task.setSubscriber(subscriber);
       pool.execute(task);
    }

   public Map<T, List<Subscriber<T>>> getSubscribers()
   {
      return subscribers;
   }

   public void unsubscribe(Cancellable<T> subscribed)
   {
      T msgType = subscribed.getTopic();
      List<Subscriber<T>> currentlySubscribed = subscribers.get(msgType);
      if (currentlySubscribed != null) {
         List<Subscriber<T>> toUnsubscribe = subscribed.getToUnsubscribe();
         currentlySubscribed.removeAll(toUnsubscribe);
         if (currentlySubscribed.isEmpty()) {
            subscribers.remove(msgType);
         }
      }
   }

   public synchronized void removeAllActionsFor(T topic) {
      subscribers.remove(topic);
   }

   public void setPrintStream(PrintStream out)
   {
      busContext.setOut(out);
   }
}
