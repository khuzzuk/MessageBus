package pl.khuzzuk.messaging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.khuzzuk.messaging.messages.CommunicateMessage;
import pl.khuzzuk.messaging.messages.RequestMessage;
import pl.khuzzuk.messaging.subscribers.Subscriber;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

class MessageWorker {
    private static final String threadsName = "Scheduler thread";
    private static final Logger log = LogManager.getLogger("MessageLogger");
    private final Map<String, List<Subscriber<? extends Message>>> subscribers;
    private final BlockingQueue<Message> channel;
    private final ExecutorService pool;

    MessageWorker(Map<String, List<Subscriber<? extends Message>>> subscribers,
                  BlockingQueue<Message> channel, ExecutorService pool) {
        this.subscribers = subscribers;
        this.channel = channel;
        this.pool = pool;
    }

    void startWorker(boolean enableLogging) {
        Thread schedulerThread = new Thread(enableLogging ? new LogScheduler() : new NoLogScheduler());
        schedulerThread.setName(threadsName);
        schedulerThread.start();
    }

    private void closeScheduler() {
        try {
            log.info("Termination of the Bus");
            if (!pool.awaitTermination(1, TimeUnit.SECONDS))
                throw new InterruptedException();
        } catch (InterruptedException e) {
            log.error("Termination of the Bus did not success.");
            log.error(e.getMessage());
            log.error(e.getStackTrace());
            pool.shutdown();
        }
    }

    private abstract class Scheduler implements Runnable {
        Message getMessage() {
            try {
                return channel.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    private class LogScheduler extends Scheduler {

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            while (true) {
                try {
                    Message message = getMessage();
                    if (message == null) {
                        continue;
                    } else if (message.getType().equals("closeBus")) {
                        closeScheduler();
                        break;
                    }
                    Collection<Subscriber<? extends Message>> subscriberCollection =
                            subscribers.get(message.getType());

                    if (subscriberCollection == null) {
                        log.error("no subscriber for " + message.getType());
                        continue;
                    }

                    for (Subscriber s : subscriberCollection) {
                        log.info("forwarded: " + message + " to: " + s);
                        pool.submit(new MessageTask(message, s));
                    }
                } catch (Exception e) {
                    log.error(e);
                    e.printStackTrace();
                }
            }
        }
    }

    private class NoLogScheduler extends Scheduler {

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            while (true) {
                try {

                    Message message = getMessage();
                    if (message == null) {
                        continue;
                    } else if (message.getType().equals("closeBus")) {
                        closeScheduler();
                        break;
                    }

                    Collection<Subscriber<? extends Message>> subscriberCollection =
                            subscribers.get(message.getType());

                    if (subscriberCollection == null) {
                        System.out.println("no subscriber for " + message.getType());
                        continue;
                    }

                    for (Subscriber s : subscriberCollection) {
                        pool.submit(new SilentMessageTask(message, s));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class MessageTask implements Runnable {
        private Message message;
        private Subscriber subscriber;

        MessageTask(Message message, Subscriber subscriber) {
            this.message = message;
            this.subscriber = subscriber;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            try {
                log.info("received message: " + message);
                subscriber.receive(message);
            } catch (Exception e) {
                log.error(describeException(e, message.getType()));
                e.printStackTrace();
            }
        }
    }

    private class SilentMessageTask implements Runnable {
        private Message message;
        private Subscriber subscriber;

        public SilentMessageTask(Message message, Subscriber subscriber) {
            this.message = message;
            this.subscriber = subscriber;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            try {
                subscriber.receive(message);
            } catch (Exception e) {
                System.err.println(describeException(e, message.getType()));
                e.printStackTrace();
                if (message.getErrorType() != null) {
                    try {
                        channel.put(new CommunicateMessage().setType(message.getErrorType()));
                    } catch (InterruptedException internalException) {
                        internalException.printStackTrace();
                    }
                }
            }
        }
    }

    private static String describeException(Exception e, String topic) {
        if (e.getClass().equals(ClassCastException.class)) {
            if (e.getMessage().contains(RequestMessage.class.getSimpleName())) {
                return "Could not create response for simple message, given topic (" +
                        topic +
                        ") is for requests but received simple message. " +
                        "Try to use bus.sendCommunicate(topic, responseTopic)" +
                        " or bus.send(topic, responseTopic, content) instead.";
            }
        }
        return e.getMessage();
    }
}
