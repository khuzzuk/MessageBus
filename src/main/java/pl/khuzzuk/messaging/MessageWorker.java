package pl.khuzzuk.messaging;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.MultiValuedMap;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Log4j2(topic = "MessageLogger")
class MessageWorker {
    private static final String threadsName = "Scheduler thread";
    private final MultiValuedMap<String, Subscriber<? extends Message>> subscribers;
    private final BlockingQueue<Message> channel;
    private final ExecutorService pool;

    MessageWorker(MultiValuedMap<String, Subscriber<? extends Message>> subscribers,
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

    void closeScheduler() {
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
                Message message = getMessage();
                if (message == null) {
                    continue;
                } else if (message.getType().equals("closeBus")) {
                    closeScheduler();
                    break;
                }
                Collection<Subscriber<? extends Message>> subscriberCollection =
                        subscribers.get(message.getType());
                for (Subscriber s : subscriberCollection) {
                    log.info("forwarded: " + message + " to: " + s);
                    pool.submit(new MessageTask(message, s));
                }
            }
        }
    }

    private class NoLogScheduler extends Scheduler {

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            while (true) {
                Message message = getMessage();
                if (message == null) {
                    continue;
                } else if (message.getType().equals("closeBus")) {
                    closeScheduler();
                    break;
                }
                Collection<Subscriber<? extends Message>> subscriberCollection =
                        subscribers.get(message.getType());
                for (Subscriber s : subscriberCollection) {
                    pool.submit(new SilentMessageTask(message, s));
                }
            }
        }
    }

    @AllArgsConstructor
    private class MessageTask implements Runnable {
        private Message message;
        private Subscriber subscriber;

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            try {
                log.info("received message: " + message);
                subscriber.receive(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @AllArgsConstructor
    private class SilentMessageTask implements Runnable {
        private Message message;
        private Subscriber subscriber;

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            try {
                subscriber.receive(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
