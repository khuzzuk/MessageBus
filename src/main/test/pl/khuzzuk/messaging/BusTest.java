package pl.khuzzuk.messaging;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.mockito.Mockito.*;

public class BusTest {

    private Bus<MessageType> bus;
    private AtomicInteger counter;

    @BeforeClass
    public void setUp() throws Exception {
        bus = Bus.initializeBus(MessageType.class, false);
    }

    @BeforeMethod
    public void beforeTest() throws Exception {
        counter = new AtomicInteger(0);
    }

    @AfterClass
    public void tearDown() throws Exception {
        bus.closeBus();
    }

    @Test
    public void publishSubscribeCheck() throws Exception {
        Object subscriber = bus.setReaction(MessageType.MESSAGE, counter::incrementAndGet);
        bus.send(MessageType.MESSAGE);

        await().pollDelay(50, MILLISECONDS).atMost(100, MILLISECONDS).until(() -> counter.get() == 1);

        bus.unSubscribe(subscriber);
    }

    @Test
    public void publishContent() {
        String content = "content";
        List<String> receiver = new ArrayList<>();

        Object subscriber = bus.<String>setReaction(MessageType.MESSAGE, receiver::add);
        bus.send(MessageType.MESSAGE, content);

        await().atMost(200, MILLISECONDS).until(() -> receiver.size()>0);
        Assert.assertEquals(content, receiver.get(0));

        bus.unSubscribe(subscriber);
    }

    @Test
    public void requestCheck() throws Exception {
        Object subscriber1 = bus.setResponse(MessageType.REQUEST, () -> {
        });
        Object subscriber2 = bus.setReaction(MessageType.RESPONSE, counter::incrementAndGet);
        bus.sendMessage(MessageType.REQUEST, MessageType.RESPONSE);

        await().atMost(200, MILLISECONDS).until(() -> counter.get() == 1);

        bus.unSubscribe(subscriber1);
        bus.unSubscribe(subscriber2);
    }

    @Test
    public void requestBagTest() throws Exception {
        Integer toAdd = 10;

        Object subscriber1 = bus.setResponse(MessageType.REQUEST, counter::addAndGet);
        Object subscriber2 = bus.setReaction(MessageType.RESPONSE, counter::addAndGet);
        bus.send(MessageType.REQUEST, MessageType.RESPONSE, toAdd);

        await().atMost(200, MILLISECONDS).until(() -> counter.get() == 20);

        bus.unSubscribe(subscriber1);
        bus.unSubscribe(subscriber2);
    }

    @Test
    public void describeExceptionWhenTryingToSendSimpleMessageForRequest() throws Exception {
        PrintStream mocked = Mockito.mock(PrintStream.class);
        System.setErr(mocked);

        Object subscriber = bus.setResponse(MessageType.MESSAGE, () -> {
        });
        bus.send(MessageType.MESSAGE);

        await().atMost(200, MILLISECONDS).until(() -> verify(mocked).println(contains(MessageType.MESSAGE.name())));

        bus.unSubscribe(subscriber);
    }

    @Test
    public void checkNoSubscriberMessage() throws Exception {
        PrintStream mocked = Mockito.mock(PrintStream.class);
        System.setErr(mocked);

        bus.send(MessageType.MESSAGE);

        await().atMost(200, MILLISECONDS).until(() -> verify(mocked).println(contains(MessageType.MESSAGE.name())));
    }

    @Test
    public void errorResponse() throws Exception {
        Object subscriber1 = bus.setResponse(MessageType.REQUEST, () -> {
            throw new IllegalStateException();
        });
        Object subscriber2 = bus.setReaction(MessageType.ERROR, counter::incrementAndGet);
        Object subscriber3 = bus.setReaction(MessageType.RESPONSE, () -> counter.addAndGet(2));

        bus.sendMessage(MessageType.REQUEST, MessageType.RESPONSE, MessageType.ERROR);

        await().atMost(200, MILLISECONDS).until(() -> counter.get() == 1);

        bus.unSubscribe(subscriber1);
        bus.unSubscribe(subscriber2);
        bus.unSubscribe(subscriber3);
    }

    @Test
    public void errorResponseWithContent() throws Exception {
        Object subscriber1 = bus.setResponse(MessageType.REQUEST, __ -> {
            throw new IllegalStateException();
        });
        Object subscriber2 = bus.setReaction(MessageType.ERROR, counter::incrementAndGet);
        Object subscriber3 = bus.setReaction(MessageType.RESPONSE, () -> counter.addAndGet(2));

        bus.send(MessageType.REQUEST, MessageType.RESPONSE, 1, MessageType.ERROR);

        await().atMost(200, MILLISECONDS).until(() -> counter.get() == 1);

        bus.unSubscribe(subscriber1);
        bus.unSubscribe(subscriber2);
        bus.unSubscribe(subscriber3);
    }

    @Test
    public void checkResponseErrorWithoutErrorTopic() throws Exception {
        RuntimeException mocked = Mockito.mock(RuntimeException.class);

        Object subscriber1 = bus.setResponse(MessageType.REQUEST, () -> {
            throw mocked;
        });
        Object subscriber2 = bus.setReaction(MessageType.RESPONSE, () -> counter.incrementAndGet());

        bus.sendMessage(MessageType.REQUEST, MessageType.RESPONSE);

        await().atMost(200, MILLISECONDS).until(() -> verify(mocked).printStackTrace());

        bus.unSubscribe(subscriber1);
        bus.unSubscribe(subscriber2);
    }

    @Test
    public void errorResponseWithContentWithoutErrorTopic() throws Exception {
        RuntimeException mocked = Mockito.mock(RuntimeException.class);

        Object subscriber1 = bus.setResponse(MessageType.REQUEST, __ -> {
            throw mocked;
        });
        Object subscriber2 = bus.setReaction(MessageType.RESPONSE, () -> counter.addAndGet(2));

        bus.send(MessageType.REQUEST, MessageType.RESPONSE, 1);

        await().atMost(200, MILLISECONDS).until(() -> verify(mocked).printStackTrace());
        Assert.assertEquals(counter.get(), 0);

        bus.unSubscribe(subscriber1);
        bus.unSubscribe(subscriber2);
    }

    @Test
    public void testUnSubscribeForMessage() throws Exception {
        PrintStream mockedStream = Mockito.mock(PrintStream.class);
        System.setErr(mockedStream);
        RuntimeException exception = mock(RuntimeException.class);

        Object subscriber1 = bus.setResponse(MessageType.REQUEST, new Action() {
            public void resolve() {
                throw exception;
            }
        });

        Object subscriber2 = bus.setReaction(MessageType.ERROR, counter::incrementAndGet);
        Object subscriber3 = bus.setReaction(MessageType.RESPONSE, () -> counter.addAndGet(2));

        bus.sendMessage(MessageType.REQUEST, MessageType.RESPONSE, MessageType.ERROR);

        await().atMost(200, MILLISECONDS).until(() -> counter.get() == 1);
        verify(exception).printStackTrace();

        bus.unSubscribe(subscriber2);

        bus.sendMessage(MessageType.REQUEST, MessageType.RESPONSE, MessageType.ERROR);

        await().atMost(200, MILLISECONDS).until(() -> counter.get() == 1);
        verify(exception, times(2)).printStackTrace();
        verify(mockedStream).println(contains(MessageType.ERROR.name()));

        bus.unSubscribe(subscriber1);

        subscriber1 = bus.setResponse(MessageType.REQUEST, () -> {
            counter.addAndGet(3);
        });

        bus.sendMessage(MessageType.REQUEST, MessageType.RESPONSE, MessageType.ERROR);

        await().atMost(200, MILLISECONDS).until(() -> counter.get() == 6);
        verify(mockedStream, never()).println(contains(MessageType.RESPONSE.name()));

        bus.unSubscribe(subscriber3);

        bus.sendMessage(MessageType.REQUEST, MessageType.RESPONSE, MessageType.ERROR);

        await().atMost(200, MILLISECONDS).until(() -> counter.get() == 9);
        verify(mockedStream).println(contains(MessageType.RESPONSE.name()));

        bus.unSubscribe(subscriber1);

        bus.sendMessage(MessageType.REQUEST, MessageType.RESPONSE, MessageType.ERROR);

        await().atMost(200, MILLISECONDS).until(
                () -> verify(mockedStream).println(contains(MessageType.REQUEST.name())));
    }
}