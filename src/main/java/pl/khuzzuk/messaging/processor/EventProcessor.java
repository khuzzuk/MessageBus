package pl.khuzzuk.messaging.processor;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import pl.khuzzuk.messaging.Cancellable;
import pl.khuzzuk.messaging.message.Message;
import pl.khuzzuk.messaging.subscriber.Subscriber;

public class EventProcessor<T extends Enum<T>> {
    final Map<T, List<pl.khuzzuk.messaging.subscriber.Subscriber<T>>> subscribers;
    final ExecutorService pool;
    final BusContext<T> busContext;

   public EventProcessor(Map<T, List<pl.khuzzuk.messaging.subscriber.Subscriber<T>>> subscribers, ExecutorService pool, PrintStream out)
   {
      this.subscribers = subscribers;
      this.pool = pool;
      busContext = new BusContext<>(out, this);
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
        List<pl.khuzzuk.messaging.subscriber.Subscriber<T>> currentSubscribers = subscribers.get(message.getTopic());
        if (currentSubscribers == null) {
            busContext.out.println(String.format("No subscribers for topic: %s", message.getTopic()));
            return;
        }

       for (pl.khuzzuk.messaging.subscriber.Subscriber<T> sub : currentSubscribers)
       {
          submitTask(message, sub);
       }
    }

    void submitTask(Message<T> message, pl.khuzzuk.messaging.subscriber.Subscriber<T> subscriber)
    {
        pool.submit(new BusTask<>(message, subscriber, busContext));
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
