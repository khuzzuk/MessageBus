package pl.khuzzuk.messaging;

import pl.khuzzuk.messaging.subscribers.ContentSubscriber;
import pl.khuzzuk.messaging.subscribers.RequestSubscriber;
import pl.khuzzuk.messaging.subscribers.Subscriber;
import pl.khuzzuk.messaging.subscribers.TransformerSubscriber;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

class EventProcessor {
    private final Map<Enum<? extends Enum<?>>, List<Subscriber>> subscribers;
    private final Map<Enum<? extends Enum<?>>, List<RequestSubscriber>> requestSubscribers;
    private final Map<Enum<? extends Enum<?>>, List<ContentSubscriber>> contentSubscribers;
    private final Map<Enum<? extends Enum<?>>, List<TransformerSubscriber>> transformerSubscribers;
    private final ExecutorService pool;

    EventProcessor(Map<Enum<? extends Enum<?>>, List<Subscriber>> subscribers,
                   Map<Enum<? extends Enum<?>>, List<ContentSubscriber>> contentSubscribers,
                   Map<Enum<? extends Enum<?>>, List<RequestSubscriber>> requestSubscribers,
                   Map<Enum<? extends Enum<?>>, List<TransformerSubscriber>> transformerSubscribers,
                   ExecutorService pool) {
        this.subscribers = subscribers;
        this.requestSubscribers = requestSubscribers;
        this.contentSubscribers = contentSubscribers;
        this.transformerSubscribers = transformerSubscribers;
        this.pool = pool;
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

    private <T> void submitTask(List<ContentSubscriber> subscribers, T content) {
        pool.submit(() -> {
            for (ContentSubscriber subscriber : subscribers) {
                subscriber.receive(content);
            }
        });
    }

    private void submitResponseTask(List<RequestSubscriber> subscribers, Enum<? extends Enum> response) {
        pool.submit(() -> {
            for (RequestSubscriber subscriber : subscribers) {
                subscriber.receive(response);
            }
        });
    }

    private <T> void submitResponseWithContentTask(List<TransformerSubscriber> subscribers, T content, Enum<? extends Enum> response) {
        pool.submit(() -> {
            for (TransformerSubscriber subscriber : subscribers) {
                subscriber.receive(content, response);
            }
        });
    }

    void processEvent(Enum<? extends Enum> topic) {
        List<Subscriber> subscribers = this.subscribers.get(topic);
        if (subscribers == null) {
            System.err.print("No subscribers for topic: ");
            System.err.println(topic);
            return;
        }
        submitTask(subscribers);
    }

    <T> void processContent(Enum<? extends Enum> topic, T content) {
        List<Subscriber> subscribers = this.subscribers.get(topic);
        if (subscribers != null) {
            submitTask(subscribers);
        }

        List<ContentSubscriber> producerSubscribers = contentSubscribers.get(topic);
        if (producerSubscribers == null) {
            System.err.print("No content subscribers for topic: ");
            System.err.println(topic);
            return;
        }
        submitTask(producerSubscribers, content);
    }

    void processRequest(Enum<? extends Enum> topic, Enum<? extends Enum> response) {
        List<Subscriber> subscribers = this.subscribers.get(topic);
        if (subscribers != null) {
            submitTask(subscribers);
        }

        List<RequestSubscriber> requestSubscribers = this.requestSubscribers.get(topic);
        if (requestSubscribers == null) {
            System.out.print("No response defined for topic: ");
            System.out.println(topic);
            return;
        }
        submitResponseTask(requestSubscribers, response);
    }

    <T> void processRequestWithContent(Enum<? extends Enum> topic, Enum<? extends Enum> response, T content) {
        List<Subscriber> subscribers = this.subscribers.get(topic);
        if (subscribers != null) {
            submitTask(subscribers);
        }

        List<RequestSubscriber> requestSubscribers = this.requestSubscribers.get(topic);
        if (requestSubscribers != null) {
            submitResponseTask(requestSubscribers, response);
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
        submitResponseWithContentTask(transformerSubscribers, content, response);
    }
}
