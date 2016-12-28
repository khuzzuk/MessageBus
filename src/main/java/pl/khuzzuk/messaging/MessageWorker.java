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

    void startWorker() {
        Thread schedulerThread = new Thread(new Scheduler());
        schedulerThread.setName(threadsName);
        schedulerThread.start();
    }

    void closeScheduler() {
        try {
            log.info("Termination of the Bus");
            pool.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Termination of the Bus did not success.");
            log.error(e.getMessage());
            log.error(e.getStackTrace());
        }
    }

    private class Scheduler implements Runnable {

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            while (true) {
                Message message = getMessage();
                if (message == null) {
                    continue;
                }
                Collection<Subscriber<? extends Message>> subscriberCollection =
                        subscribers.get(message.getType());
                for (Subscriber s : subscriberCollection) {
                    log.info("forwarded: " + message + " to: " + s);
                    pool.submit(new MessageTask(message, s));
                }
            }
        }

        private Message getMessage() {
            try {
                return channel.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
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
}