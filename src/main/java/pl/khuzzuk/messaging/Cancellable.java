package pl.khuzzuk.messaging;

import java.util.List;

import pl.khuzzuk.messaging.subscriber.Subscriber;

public class Cancellable<T extends Enum<T>>
{
   private List<Subscriber<T>> toUnsubscribe;
   private T topic;

   public Cancellable(List<Subscriber<T>> toUnsubscribe, T topic)
   {
      this.toUnsubscribe = toUnsubscribe;
      this.topic = topic;
   }

   public List<Subscriber<T>> getToUnsubscribe()
   {
      return toUnsubscribe;
   }

   public T getTopic()
   {
      return topic;
   }
}
