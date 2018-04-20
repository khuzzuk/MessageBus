package pl.khuzzuk.messaging.message;

import java.util.Queue;

import pl.khuzzuk.messaging.Action;
import pl.khuzzuk.messaging.BusPublisher;
import pl.khuzzuk.messaging.processor.EventProcessor;

public class MessageBuilder<T extends Enum<T>> implements BusPublisher<T>
{
   private EventProcessor<T> eventProcessor;
   private Message<T> message;
   private Queue<MessageBuilder<T>> returningCache;

   public MessageBuilder(EventProcessor<T> eventProcessor,
         Queue<MessageBuilder<T>> returningCache)
   {
      this.eventProcessor = eventProcessor;
      this.returningCache = returningCache;
   }

   public void setMessage(Message<T> message) {
      this.message = message;
   }

   @Override
   public void send()
   {
      eventProcessor.processEvent(message);
      message = null;
      returningCache.offer(this);
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
