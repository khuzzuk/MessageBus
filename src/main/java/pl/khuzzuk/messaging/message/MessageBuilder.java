package pl.khuzzuk.messaging.message;

import pl.khuzzuk.messaging.Action;
import pl.khuzzuk.messaging.BusPublisher;
import pl.khuzzuk.messaging.processor.EventProcessor;

public class MessageBuilder<T extends Enum<T>> implements BusPublisher<T>
{
   private EventProcessor<T> eventProcessor;
   private Message<T> message;

   public MessageBuilder(T topic, EventProcessor<T> eventProcessor)
   {
      message = new Message<>();
      message.setTopic(topic);
      this.eventProcessor = eventProcessor;
   }

   @Override
   public void send()
   {
      eventProcessor.processEvent(message);
   }

   @Override
   public <V> BusPublisher<T> withContent(V content)
   {
      message.setContent(content);
      return this;
   }

   @Override
   public BusPublisher<T> withResponse(T topic)
   {
      message.setResponseTopic(topic);
      return this;
   }

   @Override
   public BusPublisher<T> onResponse(Action action)
   {
      message.setImmediateResponse(action);
      return this;
   }

   @Override
   public BusPublisher<T> onError(Action action)
   {
      message.setOnError(action);
      return this;
   }
}
