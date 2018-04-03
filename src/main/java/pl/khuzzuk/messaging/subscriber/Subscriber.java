package pl.khuzzuk.messaging.subscriber;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import pl.khuzzuk.messaging.Action;
import pl.khuzzuk.messaging.Bus;
import pl.khuzzuk.messaging.message.Message;

public class Subscriber<T extends Enum<T>>
{
   private static final Consumer EMPTY_CONSUMER = __ -> {/*nothing*/};
   private final Bus<T> bus;
   private Action onReceive = Action.EMPTY_ACTION;
   private Consumer onPayload = EMPTY_CONSUMER;
   private Supplier onRequest;
   private Function onPayloadRequest;

   public Subscriber(Bus<T> bus)
   {
      this.bus = bus;
   }

   @SuppressWarnings("unchecked")
   public void receive(Message<T> message)
   {
      onReceive.resolve();
      onPayload.accept(message.getContent());

      if (message.getImmediateResponse() != null) {
         message.getImmediateResponse().resolve();
      }

      if (message.hasResponseTopic()) {
         if (message.hasContent() && onPayloadRequest != null){
            bus.message(message.getResponseTopic())
                  .withContent(onPayloadRequest.apply(message.getContent()))
                  .send();
         } else {
            if (onRequest != null) {
               bus.message(message.getResponseTopic())
                     .withContent(onRequest.get())
                     .send();
            } else {
               bus.message(message.getResponseTopic()).send();
            }
         }
      }
   }

   public void setOnReceive(Action onReceive)
   {
      this.onReceive = onReceive;
   }

   public void setOnPayload(Consumer onPayload)
   {
      this.onPayload = onPayload;
   }

   public void setOnRequest(Supplier onRequest)
   {
      this.onRequest = onRequest;
   }

   public void setOnPayloadRequest(Function onPayloadRequest)
   {
      this.onPayloadRequest = onPayloadRequest;
   }

   @Override
   public String toString()
   {
      String identity = onReceive != Action.EMPTY_ACTION
            ? onReceive.toString()
            : onPayload != EMPTY_CONSUMER
               ? onPayload.toString()
               : onRequest != null
                  ? onRequest.toString()
                  : onPayloadRequest != null ? onReceive.toString() : null;
      return identity;
   }
}
