package pl.khuzzuk.messaging;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface BusSubscriber<T extends Enum<T>>
{
   Cancellable<T> subscribe();

   BusSubscriber<T> then(Action action);

   <U> BusSubscriber<T> accept(Consumer<U> consumer);

   <U, V> BusSubscriber<T> mapResponse(Function<U, V> action);

   <U> BusSubscriber<T> withResponse(Supplier<U> contentSupplier);

   BusSubscriber<T> onFXThread();

   BusSubscriber<T> onBusThread();
}
