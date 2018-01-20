package pl.khuzzuk.messaging;

import pl.khuzzuk.messaging.subscribers.ContentSubscriber;
import pl.khuzzuk.messaging.subscribers.RequestSubscriber;
import pl.khuzzuk.messaging.subscribers.Subscriber;
import pl.khuzzuk.messaging.subscribers.TransformerSubscriber;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

class EventProcessor<T extends Enum<T>> {
    private final Map<T, List<Subscriber>> subscribers;
    private final Map<T, List<RequestSubscriber>> requestSubscribers;
    private final Map<T, List<ContentSubscriber>> contentSubscribers;
    private final Map<T, List<TransformerSubscriber>> transformerSubscribers;
    private final ExecutorService pool;
    private final Class<T> messageTypes;

    EventProcessor(Map<T, List<Subscriber>> subscribers,
                   Map<T, List<RequestSubscriber>> requestSubscribers,
                   Map<T, List<ContentSubscriber>> contentSubscribers,
                   Map<T, List<TransformerSubscriber>> transformerSubscribers,
                   ExecutorService pool,
                   Class<T> messageTypes) {
        this.subscribers = subscribers;
        this.requestSubscribers = requestSubscribers;
        this.contentSubscribers = contentSubscribers;
        this.transformerSubscribers = transformerSubscribers;
        this.pool = pool;
        this.messageTypes = messageTypes;
    }

    void close() {
        try {
            pool.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            pool.shutdownNow();
        }
        pool.shutdownNow();
    }

    private void submitTask(List<Subscriber> subscribers) {
        pool.submit(() -> subscribers.forEach(Subscriber::receive));
    }

    private <V> void submitTask(List<ContentSubscriber> subscribers, V content) {
        pool.submit(() -> {
            for (ContentSubscriber subscriber : subscribers) {
                subscriber.receive(content);
            }
        });
    }

    private void submitResponseTask(List<RequestSubscriber> subscribers, T response, T errorTopic) {
        pool.submit(() -> {
            for (RequestSubscriber subscriber : subscribers) {
                subscriber.receive(response, errorTopic);
            }
        });
    }

    private <V> void submitResponseWithContentTask(List<TransformerSubscriber> subscribers,
                                                   V content, T response, T errorTopic) {
        pool.submit(() -> {
            for (TransformerSubscriber subscriber : subscribers) {
                subscriber.receive(content, response, errorTopic);
            }
        });
    }

    void processEvent(T topic) {
        List<Subscriber> subscribers = this.subscribers.get(topic);
        if (subscribers == null) {
            System.err.println(String.format("No subscribers for topic: %s", topic));
            return;
        }
        submitTask(subscribers);
    }

    <V> void processContent(T topic, V content) {
        List<Subscriber> subscribers = this.subscribers.get(topic);
        if (subscribers != null) {
            submitTask(subscribers);
        }

        List<ContentSubscriber> producerSubscribers = contentSubscribers.get(topic);
        if (producerSubscribers == null) {
            System.err.println(String.format("No content subscribers for topic: %s", topic));
            return;
        }
        submitTask(producerSubscribers, content);
    }

    void processRequest(T topic, T response, T errorTopic) {
        List<Subscriber> subscribers = this.subscribers.get(topic);
        if (subscribers != null) {
            submitTask(subscribers);
        }

        List<RequestSubscriber> requestSubscribers = this.requestSubscribers.get(topic);
        if (requestSubscribers == null) {
            System.err.println(String.format("No response defined for topic: %s", topic));
            return;
        }
        submitResponseTask(requestSubscribers, response, errorTopic);
    }

    <V> void processRequestWithContent(T topic, T response, V content, T errorTopic) {
        List<Subscriber> subscribers = this.subscribers.get(topic);
        if (subscribers != null) {
            submitTask(subscribers);
        }

        List<RequestSubscriber> requestSubscribers = this.requestSubscribers.get(topic);
        if (requestSubscribers != null) {
            submitResponseTask(requestSubscribers, response, errorTopic);
        }

        List<ContentSubscriber> producerSubscribers = contentSubscribers.get(topic);
        if (producerSubscribers != null) {
            submitTask(producerSubscribers, content);
        }

        List<TransformerSubscriber> transformerSubscribers = this.transformerSubscribers.get(topic);
        if (transformerSubscribers == null) {
            System.err.print("No response mapper defined for topic: ");
            System.err.println(topic);
            return;
        }
        submitResponseWithContentTask(transformerSubscribers, content, response, errorTopic);
    }
}
