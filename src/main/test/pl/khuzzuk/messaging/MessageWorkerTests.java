package pl.khuzzuk.messaging;

import com.jayway.awaitility.Awaitility;
import org.testng.Assert;
import org.testng.annotations.Test;
import pl.khuzzuk.messaging.messages.CommunicateMessage;
import pl.khuzzuk.messaging.subscribers.Subscriber;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

public class MessageWorkerTests {
    @Test
    public void closingWorkers() throws Exception {
        ArrayBlockingQueue<Message> channel = new ArrayBlockingQueue<>(3);
        ExecutorService pool = Executors.newFixedThreadPool(3);
        HashMap<String, List<Subscriber<? extends Message>>> subscribers = new HashMap<>();
        Bus bus = new Bus(channel, subscribers);

        ExecutorService mockedPool = mock(ExecutorService.class);
        when(mockedPool.awaitTermination(anyInt(), any(TimeUnit.class))).thenAnswer(i -> pool.awaitTermination(1, TimeUnit.SECONDS));
        when(mockedPool.submit(any(Runnable.class))).then(i -> pool.submit((Runnable) i.getArguments()[0]));
        MessageWorker worker = new MessageWorker(subscribers,
                channel,
                pool);
        worker.startWorker(false);

        AtomicInteger a = new AtomicInteger(0);
        bus.setReaction("a", a::incrementAndGet);
        for (int x = 0; x < 100; x ++) {
            bus.send("a");
        }

        Awaitility.await().atMost(2, TimeUnit.SECONDS).pollDelay(100, TimeUnit.MILLISECONDS)
                .until(() -> a.get() == 100);

        Assert.assertFalse(pool.isShutdown());

        channel.put(new CommunicateMessage().setType("closeBus"));
        Awaitility.await()
                .atMost(2, TimeUnit.SECONDS)
                .pollDelay(100, TimeUnit.MILLISECONDS)
                .until(pool::isTerminated);

        verify(mockedPool, never()).shutdown();
        Assert.assertTrue(pool.isShutdown());
    }
}
