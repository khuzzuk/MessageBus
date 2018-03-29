package pl.khuzzuk.messaging;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import pl.khuzzuk.messaging.processor.EventProcessor;
import pl.khuzzuk.messaging.processor.Node;
import pl.khuzzuk.messaging.subscriber.GuiSubscriber;
import pl.khuzzuk.messaging.subscriber.Subscriber;

public class BusSubscriberProcessor<T extends Enum<T>> implements BusSubscriber<T>
{
   private final T topic;
   private final Bus<T> bus;
   private final EventProcessor<T> eventProcessor;

   private Node<T> subscribers;
   private boolean onFxThread;

   BusSubscriberProcessor(T topic, Bus<T> bus, EventProcessor<T> eventProcessor)
   {
      this.topic = topic;
      this.bus = bus;
      this.eventProcessor = eventProcessor;
   }

   @Override
   public Cancellable<T> subscribe()
   {
      List<Subscriber<T>> objectsToUnsubscribe = new LinkedList<>();
      if (subscribers == null) throw new IllegalStateException("Cannot subscribe when no subscription is defined");

      subscribers.addTo(eventProcessor.getSubscribers(), objectsToUnsubscribe);
      return new Cancellable<>(objectsToUnsubscribe, topic);
   }

   @Override
   public BusSubscriber<T> then(Action action)
   {
      Subscriber<T> subscriber = createSubscriber();
      subscriber.setOnReceive(action);
      addNode(new Node<>(subscriber, topic));
      return this;
   }

   @Override
   public <U> BusSubscriber<T> accept(Consumer<U> consumer)
   {
      Subscriber<T> subscriber = createSubscriber();
      subscriber.setOnPayload(consumer);
      addNode(new Node<>(subscriber, topic));
      return this;
   }

   @Override
   public <U, V> BusSubscriber<T> mapResponse(Function<U, V> mapper)
   {
      Subscriber<T> subscriber = createSubscriber();
      subscriber.setOnPayloadRequest(mapper);
      addNode(new Node<>(subscriber, topic));
      return this;
   }

   @Override
   public <U> BusSubscriber<T> withResponse(Supplier<U> contentSupplier)
   {
      Subscriber<T> subscriber = createSubscriber();
      subscriber.setOnRequest(contentSupplier);
      addNode(new Node<>(subscriber, topic));
      return this;
   }

   @Override
   public BusSubscriber<T> onFXThread()
   {
      onFxThread = true;
      return this;
   }

   @Override
   public BusSubscriber<T> onBusThread()
   {
      onFxThread = false;
      return this;
   }

   private Subscriber<T> createSubscriber()
   {
      return onFxThread ? new GuiSubscriber<>(bus) : new Subscriber<>(bus);
   }

   private void addNode(Node<T> toAdd)
   {
      if (subscribers == null) subscribers = toAdd;
      else subscribers.addLast(toAdd);
   }
}
