package pl.khuzzuk.messaging;

public interface BusPublisher<T extends Enum<T>>
{
   void send();

   <V> BusPublisher<T> withContent(V content);

   BusPublisher<T> withResponse(T topic);

   BusPublisher<T> onResponse(Action action);

   BusPublisher<T> onError(Action action);
}
