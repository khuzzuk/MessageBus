package pl.khuzzuk.messaging;

import java.io.PrintStream;
import java.util.EnumMap;
import java.util.concurrent.Executors;

import pl.khuzzuk.messaging.message.MessageBuilder;
import pl.khuzzuk.messaging.processor.EventProcessor;

public class Bus<T extends Enum<T>> {
    private final EventProcessor<T> eventProcessor;

    private Bus(EventProcessor<T> eventProcessor)
    {
        this.eventProcessor = eventProcessor;
    }

    @SuppressWarnings("unused")
    public static <T extends Enum<T>> Bus<T> initializeBus(Class<T> enumType) {
        return initializeBus(enumType, System.out);
    }

    @SuppressWarnings("WeakerAccess")
    public static <T extends Enum<T>> Bus<T> initializeBus(Class<T> enumType, PrintStream out) {
        return initializeBus(enumType, out, 3);
    }

    @SuppressWarnings("WeakerAccess")
    public static <T extends Enum<T>> Bus<T> initializeBus(Class<T> enumType, PrintStream out, int threads) {
        EventProcessor<T> enumEventProcessor = new EventProcessor<>(
              new EnumMap<>(enumType),
              Executors.newFixedThreadPool(threads),
              out);

        return new Bus<>(enumEventProcessor);
    }

    @SuppressWarnings({"unused", "unchecked"})
    public void unSubscribe(Cancellable<T> subscriber) {
        try {
            eventProcessor.unsubscribe(subscriber);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Provided subscriber cannot be unsubscribed. Most likely because it is not the object returned from Bus methods");
        }
    }

    @SuppressWarnings("unused")
    public void closeBus() {
        eventProcessor.close();
    }

    @SuppressWarnings("unused")
    public void removeAllActionsFor(T topic) {
        eventProcessor.removeAllActionsFor(topic);
    }

    @SuppressWarnings("unused")
    public BusSubscriber<T> subscribingFor(T message) {
       return new BusSubscriberProcessor<>(message, this, eventProcessor);
    }

    @SuppressWarnings("unused")
    public BusPublisher<T> message(T message)
    {
        return new MessageBuilder<>(message, eventProcessor);
    }

    void setPrintStream(PrintStream out)
    {
        eventProcessor.setPrintStream(out);
    }
}
