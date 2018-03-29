package pl.khuzzuk.messaging.processor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import pl.khuzzuk.messaging.subscriber.Subscriber;

public class Node<T extends Enum<T>>
{
   private Subscriber<T> element;
   private T message;
   private Node<T> next;

   public Node(Subscriber<T> element, T message)
   {
      this.element = element;
      this.message = message;
   }

   public void addTo(Map<T, List<Subscriber<T>>> subscribers, List<Subscriber<T>> objectsToUnsubscribe)
   {
      objectsToUnsubscribe.add(element);
      subscribers.computeIfAbsent(message, t -> new CopyOnWriteArrayList<>()).add(element);
      if (next != null)
      {
         next.addTo(subscribers, objectsToUnsubscribe);
      }
   }

   public void addLast(Node<T> lastNode)
   {
      if (next == null)
      {
         next = lastNode;
      }
      else
      {
         next.addLast(lastNode);
      }
   }
}
