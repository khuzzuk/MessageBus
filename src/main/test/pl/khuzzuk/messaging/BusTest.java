package pl.khuzzuk.messaging;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class BusTest {

    private Bus bus;

    @BeforeMethod
    public void setUp() throws Exception {
        bus = Bus.initializeBus(false);
    }

    @Test
    public void publishSubscribeCheck() throws Exception {
        String msg = "simple check";
        AtomicInteger x = new AtomicInteger(0);

        bus.setReaction(msg, x::incrementAndGet);
        bus.send(msg);

        await().pollDelay(50, MILLISECONDS).atMost(100, MILLISECONDS).until(() -> x.get() == 1);
    }

    @Test
    public void publishContent() {
        String msg = "content check";
        String content = "content";
        List<String> receiver = new ArrayList<>();

        bus.<String>setReaction(msg, receiver::add);
        bus.send(msg, content);

        await().atMost(200, MILLISECONDS).until(() -> receiver.size()>0);
        Assert.assertEquals(content, receiver.get(0));
    }

    @Test
    public void requestCheck() throws Exception {
        String msg = "request check";
        String response = "response check";
        AtomicInteger counter = new AtomicInteger(0);

        bus.setResponse(msg, () -> {});
        bus.setReaction(response, counter::incrementAndGet);
        bus.sendCommunicate(msg, response);

        await().atMost(200, MILLISECONDS).until(() -> counter.get() == 1);
    }

    @Test
    public void requestBagTest() throws Exception {
        AtomicInteger i = new AtomicInteger(0);
        String msg = "requestBagTest";
        String response = "responseBagTest";
        Integer toAdd = 10;

        bus.setResponse(msg, i::addAndGet);
        bus.setReaction(response, i::addAndGet);
        bus.publish(bus.getBagRequest(msg, response, toAdd));

        await().atMost(200, MILLISECONDS).until(() -> i.get() == 20);
    }

    @Test
    public void describeExceptionWhenTryingToSendSimpleMessageForRequest() throws Exception {
        String msg = "msg";
        PrintStream mocked = Mockito.mock(PrintStream.class);
        System.setErr(mocked);

        bus.setResponse(msg, () -> {});
        bus.send(msg);

        await().atMost(200, MILLISECONDS).until(() -> Mockito.verify(mocked).println(Mockito.anyString()));
    }

    @Test
    public void checkNoSubscriberMessage() throws Exception {
        PrintStream mocked = Mockito.mock(PrintStream.class);
        System.setOut(mocked);

        bus.send("msg");

        await().atMost(200, MILLISECONDS).until(() -> Mockito.verify(mocked).println(Mockito.anyString()));
    }

    @Test
    public void errorResponse() throws Exception {
        String msg = "msg";
        String res = "res";
        String err = "err";
        AtomicInteger counter = new AtomicInteger(0);

        bus.setResponse(msg, () -> {
            throw new IllegalStateException();
        });
        bus.setReaction(err, counter::incrementAndGet);
        bus.setReaction(res, () -> counter.addAndGet(2));

        bus.sendCommunicate(msg, res, err);

        await().atMost(200, MILLISECONDS).until(() -> counter.get() == 1);
    }

    @Test
    public void errorResponseWithContent() throws Exception {
        String msg = "msg";
        String res = "res";
        String err = "err";
        AtomicInteger counter = new AtomicInteger(0);

        bus.setResponse(msg, __ -> {
            throw new IllegalStateException();
        });
        bus.setReaction(err, counter::incrementAndGet);
        bus.setReaction(res, () -> counter.addAndGet(2));

        bus.send(msg, res, 1, err);

        await().atMost(200, MILLISECONDS).until(() -> counter.get() == 1);
    }
}