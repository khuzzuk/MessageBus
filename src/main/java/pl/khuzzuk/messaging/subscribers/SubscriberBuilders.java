package pl.khuzzuk.messaging.subscribers;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import pl.khuzzuk.messaging.Action;
import pl.khuzzuk.messaging.Bus;

public class SubscriberBuilders
{
   public static <T extends Enum<T>> Subscriber subscriber(boolean onFxThread, T message, Action action)
   {
      SimpleSubscriber simpleSubscriber = onFxThread ? new GuiSimpleSubscriber() : new SimpleSubscriber();
      simpleSubscriber.setMessageType(message);
      simpleSubscriber.setAction(action);
      return simpleSubscriber;
   }

   public static <T extends Enum<T>> RequestSubscriber requestSubscriber(boolean onFxThread, T message, Action action, Bus<T> bus)
   {
      RequestSubscriber subscriber = onFxThread ? new GuiRequestSubscriber(bus) : new RequestMessageSubscriber(bus);
      subscriber.setMessageType(message);
      subscriber.setAction(action);
      return subscriber;
   }

   public static <T extends Enum<T>, U> ContentSubscriber contentSubscriber(boolean onFxThread, T message, Consumer<U> consumer) {
      AbstractContentSubscriber subscriber = onFxThread ? new GuiContentSubscriber(message) : new BagSubscriber(message);
      subscriber.setConsumer(consumer);
      return subscriber;
   }

   public static <T extends Enum<T>, U, V> TransformerSubscriber transformerSubscriber(
         boolean onFxThread, T message, Function<U, V> transformer, Bus<T> bus)
   {
      TransformerSubscriber subscriber = onFxThread
            ? new GuiRequestBagSubscriber(bus, message)
            : new RequestBagSubscriber(bus, message);
      subscriber.setResponseResolver(transformer);
      return subscriber;
   }

   public static <T extends Enum<T>, U> RequestProducerSubscriber requestProducerSubscriber(
         boolean onFxThread, T message, Supplier<U> contentSupplier, Bus<T> bus)
   {
      RequestProducerSubscriber subscriber = onFxThread
            ? new GuiRequestProducerSubscriber(bus)
            : new RequestProducerSubscriber(bus);
      subscriber.setMessageType(message);
      subscriber.setResponseProducer(contentSupplier);
      return subscriber;
   }
}
