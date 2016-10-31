package pl.khuzzuk.messaging;

import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class BusTest {

    private Bus bus;

    @BeforeSuite
    public void setUp() throws Exception {
        bus = Bus.initializeBus();
    }

    @Test
    public void publishSubscribeCheck() throws Exception {
        String msg = "simple check";
        AtomicInteger x = new AtomicInteger(0);
        Publisher<Message> publisher = bus.getPublisher();
        Subscriber<Message> subscriber = bus.getSubscriber(msg, () -> x.addAndGet(1));
        publisher.publish(new CommunicateMessage().setType(msg));
        await().pollDelay(50, MILLISECONDS).atMost(100, MILLISECONDS).until(() -> x.get() == 1);
        Assert.assertTrue(x.get() == 1);
    }

    @Test
    public void publishContent() {
        BagPublisher<String> publisher = bus.getBagPublisher();
        String msg = "content check";
        String content = "content";
        List<String> receiver = new ArrayList<>();
        ContentSubscriber<String, BagMessage<String>> subscriber = bus.getContentSubscriber(msg, receiver::add);
        publisher.publish(content, msg);
        await().atMost(200, MILLISECONDS).until(() -> receiver.size()>0);
        Assert.assertEquals(content, receiver.get(0));
    }

    @Test
    public void requestCheck() throws Exception {
        Publisher<RequestMessage> publisher = bus.getPublisher();
        String msg = "request check";
        String response = "response check";
        List<String> receiver = new ArrayList<>();
        Subscriber<RequestMessage> subscriber = bus.getRequestSubscriber(msg, () -> receiver.add(""));
        Subscriber<Message> responseSubscriber = bus.getSubscriber(response, () -> receiver.add(""));
        publisher.publish(bus.getRequest(msg, response));
        await().atMost(200, MILLISECONDS).until(() -> receiver.size()==2);
    }

    @Test
    public void requestBagTest() throws Exception {
        AtomicInteger i = new AtomicInteger(0);
        String msg = "requestBagTest";
        String response = "responseBagTest";
        Integer toAdd = 1;
        bus.setResponseResolver(msg, i::addAndGet);
        bus.setReaction(response, i::addAndGet);
        bus.publish(bus.getBagRequest(msg, response, toAdd));
        await().atMost(200, MILLISECONDS).until(() -> i.get() == 2);
    }
}