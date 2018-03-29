package pl.khuzzuk.messaging.message;

import pl.khuzzuk.messaging.Action;

public class Message<T extends Enum<T>>
{

   private T topic;
   private T responseTopic;
   private Action immediateResponse;
   private Action onError;
   private Object content;

   public boolean hasContent()
   {
      return content != null;
   }

   public boolean hasResponseTopic()
   {
      return responseTopic != null;
   }

   public T getTopic()
   {
      return topic;
   }

   public void setTopic(T topic)
   {
      this.topic = topic;
   }

   public T getResponseTopic()
   {
      return responseTopic;
   }

   public void setResponseTopic(T responseTopic)
   {
      this.responseTopic = responseTopic;
   }

   public Action getImmediateResponse()
   {
      return immediateResponse;
   }

   public void setImmediateResponse(Action immediateResponse)
   {
      this.immediateResponse = immediateResponse;
   }

   public Action getOnError()
   {
      return onError;
   }

   public void setOnError(Action onError)
   {
      this.onError = onError;
   }

   public Object getContent()
   {
      return content;
   }

   public void setContent(Object content)
   {
      this.content = content;
   }
}
