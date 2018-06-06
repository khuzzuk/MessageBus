package pl.khuzzuk.messaging;

import java.io.PrintStream;
import java.util.EnumMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import pl.khuzzuk.messaging.message.Message;
import pl.khuzzuk.messaging.message.MessageBuilder;
import pl.khuzzuk.messaging.processor.BusTask;
import pl.khuzzuk.messaging.processor.BusWorkerPool;
import pl.khuzzuk.messaging.processor.EventProcessor;
import pl.khuzzuk.messaging.processor.LoggingEventProcessor;
import pl.khuzzuk.messaging.subscriber.Subscriber;

public class Bus<T extends Enum<T>> {
    private final EventProcessor<T> eventProcessor;
    private final Queue<Message<T>> messagesCache;
    private final Queue<MessageBuilder<T>> messageBuildersCache;

    private Bus(EventProcessor<T> eventProcessor, Queue<Message<T>> messagesCache,
          Queue<MessageBuilder<T>> messageBuildersCache)
    {
        this.eventProcessor = eventProcessor;
        this.messagesCache = messagesCache;
        this.messageBuildersCache = messageBuildersCache;
    }

    @SuppressWarnings("unused")
    public static <T extends Enum<T>> Bus<T> initializeBus(Class<T> enumType) {
        return initializeBus(enumType, System.out); //NOSONAR
    }

    @SuppressWarnings("WeakerAccess")
    public static <T extends Enum<T>> Bus<T> initializeBus(Class<T> enumType, PrintStream out) {
        return initializeBus(enumType, out, false);
    }

    @SuppressWarnings("WeakerAccess")
    public static <T extends Enum<T>> Bus<T> initializeBus(Class<T> enumType, PrintStream out, boolean loggingMessages) {
        return initializeBus(enumType, out, loggingMessages, 3);
    }

    @SuppressWarnings("WeakerAccess")
    public static <T extends Enum<T>> Bus<T> initializeBus(Class<T> enumType, PrintStream out, boolean loggingMessages, int threads) {
        EnumMap<T, List<Subscriber<T>>> enumMap = new EnumMap<>(enumType);
        BusWorkerPool<T> pool = BusWorkerPool.start(threads);
        Queue<Message<T>> messages = new ArrayBlockingQueue<>(threads * 2);
        Queue<? extends BusTask<T>> tasks = new ArrayBlockingQueue<>(threads * 2);

        EventProcessor<T> enumEventProcessor = loggingMessages
              ? new LoggingEventProcessor<>(enumMap, pool, out, messages, tasks)
              : new EventProcessor<>(enumMap, pool, out, messages, tasks);

        return new Bus<>(enumEventProcessor,
              messages,
              new ArrayBlockingQueue<>(threads * 2));
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
    public BusPublisher<T> message(T topic)
    {
        Message<T> message = messagesCache.poll();
        if (message == null) message = new Message<>();
        message.setTopic(topic);
        MessageBuilder<T> messageBuilder = messageBuildersCache.poll();
        if (messageBuilder == null) {
            messageBuilder = new MessageBuilder<>(eventProcessor, messageBuildersCache);
        }
        messageBuilder.setMessage(message);
        return messageBuilder;
    }

    void setPrintStream(PrintStream out)
    {
        eventProcessor.setPrintStream(out);
    }
}
