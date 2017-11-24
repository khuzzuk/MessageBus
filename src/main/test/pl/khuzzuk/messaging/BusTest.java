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
        AtomicInteger x = new AtomicInteger(0);

        bus.setReaction(MessageType.COMMUNICATE, x::incrementAndGet);
        bus.send(MessageType.COMMUNICATE);

        await().pollDelay(50, MILLISECONDS).atMost(100, MILLISECONDS).until(() -> x.get() == 1);
    }

    @Test
    public void publishContent() {
        String content = "content";
        List<String> receiver = new ArrayList<>();

        bus.<String>setReaction(MessageType.COMMUNICATE, receiver::add);
        bus.send(MessageType.COMMUNICATE, content);

        await().atMost(200, MILLISECONDS).until(() -> receiver.size()>0);
        Assert.assertEquals(content, receiver.get(0));
    }

    @Test
    public void requestCheck() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);

        bus.setResponse(MessageType.REQUEST, () -> {});
        bus.setReaction(MessageType.RESPONSE, counter::incrementAndGet);
        bus.sendCommunicate(MessageType.REQUEST, MessageType.RESPONSE);

        await().atMost(200, MILLISECONDS).until(() -> counter.get() == 1);
    }

    @Test
    public void requestBagTest() throws Exception {
        AtomicInteger i = new AtomicInteger(0);
        Integer toAdd = 10;

        bus.setResponse(MessageType.REQUEST, i::addAndGet);
        bus.setReaction(MessageType.RESPONSE, i::addAndGet);
        bus.send(MessageType.REQUEST, MessageType.RESPONSE, toAdd);

        await().atMost(200, MILLISECONDS).until(() -> i.get() == 20);
    }

    @Test
    public void describeExceptionWhenTryingToSendSimpleMessageForRequest() throws Exception {
        PrintStream mocked = Mockito.mock(PrintStream.class);
        System.setErr(mocked);

        bus.setResponse(MessageType.COMMUNICATE, () -> {});
        bus.send(MessageType.COMMUNICATE);

        await().atMost(200, MILLISECONDS).until(() -> Mockito.verify(mocked).println(MessageType.COMMUNICATE));
    }

    @Test
    public void checkNoSubscriberMessage() throws Exception {
        PrintStream mocked = Mockito.mock(PrintStream.class);
        System.setErr(mocked);

        bus.send(MessageType.COMMUNICATE);

        await().atMost(200, MILLISECONDS).until(() -> Mockito.verify(mocked).println(MessageType.COMMUNICATE));
    }

    public void errorResponse() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);

        bus.setResponse(MessageType.REQUEST, () -> {
            throw new IllegalStateException();
        });
        bus.setReaction(MessageType.ERROR, counter::incrementAndGet);
        bus.setReaction(MessageType.RESPONSE, () -> counter.addAndGet(2));

        bus.sendCommunicate(MessageType.REQUEST, MessageType.RESPONSE, MessageType.ERROR);

        await().atMost(200, MILLISECONDS).until(() -> counter.get() == 1);
    }

    public void errorResponseWithContent() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);

        bus.setResponse(MessageType.REQUEST, __ -> {
            throw new IllegalStateException();
        });
        bus.setReaction(MessageType.ERROR, counter::incrementAndGet);
        bus.setReaction(MessageType.RESPONSE, () -> counter.addAndGet(2));

        bus.send(MessageType.REQUEST, MessageType.RESPONSE, 1, MessageType.ERROR);

        await().atMost(200, MILLISECONDS).until(() -> counter.get() == 1);
    }

    enum MessageType {
        COMMUNICATE, ERROR, REQUEST, RESPONSE;
    }
}