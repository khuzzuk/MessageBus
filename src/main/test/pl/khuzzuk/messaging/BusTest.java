package pl.khuzzuk.messaging;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static pl.khuzzuk.messaging.MessageType.REQUEST;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pl.khuzzuk.messaging.subscriber.Subscriber;

public class BusTest {

    private Bus<MessageType> bus;
    private AtomicInteger counter;
    private PrintStream out;

    @BeforeClass
    public void setUp() {
        bus = Bus.initializeBus(MessageType.class, out, false, 1);
    }

    @BeforeMethod
    public void beforeTest() {
        out = Mockito.mock(PrintStream.class);
        bus.setPrintStream(out);
        counter = new AtomicInteger(0);
    }

    @AfterClass
    public void tearDown() {
        bus.closeBus();
    }

    @Test
    public void publishSubscribeCheck() {
        Cancellable<MessageType> subscriber = bus.subscribingFor(MessageType.MESSAGE)
              .then(counter::incrementAndGet).subscribe();
        bus.message(MessageType.MESSAGE).send();

        await().pollDelay(50, MILLISECONDS).atMost(100, MILLISECONDS).until(() -> counter.get() == 1);

        bus.unSubscribe(subscriber);
    }

    @Test
    public void publishContent() {
        String content = "content";
        List<String> receiver = new ArrayList<>();

        Cancellable<MessageType> subscriber = bus.subscribingFor(MessageType.MESSAGE)
              .<String>accept(receiver::add).subscribe();
        bus.message(MessageType.MESSAGE).withContent(content).send();

        await().atMost(200, MILLISECONDS).until(() -> receiver.size()>0);
        Assert.assertEquals(content, receiver.get(0));

        bus.unSubscribe(subscriber);
    }

    @Test
    public void requestCheck() {
        Cancellable<MessageType> subscriber1 = bus.subscribingFor(REQUEST).then(Subscriber.EMPTY_ACTION).subscribe();
        Cancellable<MessageType> subscriber2 = bus.subscribingFor(MessageType.RESPONSE).then(counter::incrementAndGet).subscribe();
        bus.message(REQUEST).withResponse(MessageType.RESPONSE).send();

        await().atMost(200, MILLISECONDS).until(() -> counter.get() == 1);

        bus.unSubscribe(subscriber1);
        bus.unSubscribe(subscriber2);
    }

    @Test
    public void requestBagTest() {
        Integer toAdd = 10;

        Cancellable<MessageType> subscriber1 = bus.subscribingFor(REQUEST).mapResponse(counter::addAndGet).subscribe();
        Cancellable<MessageType> subscriber2 = bus.subscribingFor(MessageType.RESPONSE).accept(counter::addAndGet).subscribe();

        bus.message(REQUEST).withResponse(MessageType.RESPONSE).withContent(toAdd).send();

        await().atMost(200, MILLISECONDS).until(() -> counter.get() == 20);

        bus.unSubscribe(subscriber1);
        bus.unSubscribe(subscriber2);
    }

    @Test
    public void checkNoSubscriberMessage() {
        bus.message(MessageType.MESSAGE).send();

        await().atMost(200, MILLISECONDS).until(() -> verify(out).println(contains(MessageType.MESSAGE.name())));
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void describeExceptionWhenTryingToSubscribeWithoutAction() {
        bus.subscribingFor(MessageType.MESSAGE).subscribe();
    }

    @Test
    public void errorResponse() {
        Cancellable<MessageType> subscriber1 = bus.subscribingFor(REQUEST).then(() -> {
            throw new IllegalStateException();
        }).subscribe();
        Cancellable<MessageType> subscriber2 = bus.subscribingFor(MessageType.RESPONSE).then(() -> counter.addAndGet(2)).subscribe();

        bus.message(REQUEST)
              .withResponse(MessageType.RESPONSE)
              .onError(counter::incrementAndGet)
              .send();

        await().atMost(200, MILLISECONDS).until(() -> counter.get() == 1);
        verify(out).println(contains(REQUEST.name()));
        verify(out).println(any(IllegalStateException.class));

        bus.unSubscribe(subscriber1);
        bus.unSubscribe(subscriber2);
        bus.removeAllActionsFor(MessageType.ERROR);
    }

    @Test
    public void errorResponseWithContent() {
        Cancellable<MessageType> subscriber1 = bus.subscribingFor(REQUEST).accept(__ -> {
            throw new IllegalStateException();
        }).subscribe();
        Cancellable<MessageType> subscriber2 = bus.subscribingFor(MessageType.RESPONSE).then(() -> counter.addAndGet(2)).subscribe();

        bus.message(REQUEST)
              .withResponse(MessageType.RESPONSE)
              .withContent(1)
              .onError(counter::incrementAndGet).send();

        await().atMost(200, MILLISECONDS).until(() -> counter.get() == 1);

        bus.unSubscribe(subscriber1);
        bus.unSubscribe(subscriber2);
        bus.removeAllActionsFor(MessageType.ERROR);
    }

    @Test
    public void checkResponseErrorWithoutErrorTopic() {
        RuntimeException mocked = Mockito.mock(RuntimeException.class);

        Cancellable<MessageType> subscriber1 = bus.subscribingFor(REQUEST).then(() -> {
            throw mocked;
        }).subscribe();

        Cancellable<MessageType> subscriber2 = bus.subscribingFor(MessageType.RESPONSE).then(counter::incrementAndGet).subscribe();

        bus.message(REQUEST).withResponse(MessageType.RESPONSE).send();

        await().atMost(200, MILLISECONDS).until(() -> verify(out).println(contains(REQUEST.name())));
        verify(mocked).printStackTrace(out);

        bus.unSubscribe(subscriber1);
        bus.unSubscribe(subscriber2);
    }

    @Test
    public void errorResponseWithContentWithoutErrorTopic() {
        RuntimeException mocked = Mockito.mock(RuntimeException.class);

        Cancellable<MessageType> subscriber1 = bus.subscribingFor(REQUEST).mapResponse(__ -> {
            throw mocked;
        }).subscribe();
        Cancellable<MessageType> subscriber2 = bus.subscribingFor(MessageType.RESPONSE).then(() -> counter.addAndGet(2)).subscribe();

        bus.message(REQUEST).withResponse(MessageType.RESPONSE).withContent(1).send();

        await().atMost(200, MILLISECONDS).until(() -> verify(mocked).printStackTrace(out));
        Assert.assertEquals(counter.get(), 0);

        bus.unSubscribe(subscriber1);
        bus.unSubscribe(subscriber2);
    }

    @Test
    public void testMultipleMapResponses() {
        Cancellable<MessageType> subscriber1 = bus.subscribingFor(REQUEST).mapResponse(counter::addAndGet).subscribe();
        Cancellable<MessageType> subscriber2 = bus.subscribingFor(MessageType.RESPONSE).accept((Integer i) -> counter.set(1)).subscribe();
        Cancellable<MessageType> subscriber3 = bus.subscribingFor(MessageType.SECONDARY_RESPONSE).accept((Integer i) -> counter.set(2)).subscribe();

        bus.message(REQUEST).withResponse(MessageType.RESPONSE).withContent(0).send();

        await().atMost(200, MILLISECONDS).until(() -> counter.get() == 1);

        bus.message(REQUEST).withResponse(MessageType.SECONDARY_RESPONSE).withContent(0).send();

        await().atMost(200, MILLISECONDS).until(() -> counter.get() == 2);

        bus.unSubscribe(subscriber1);
        bus.unSubscribe(subscriber2);
        bus.unSubscribe(subscriber3);
    }

    @Test
    public void testImmidiateResponseWithContentInOriginalMessage() {
        Cancellable<MessageType> subscriber = bus.subscribingFor(REQUEST).accept(counter::addAndGet).subscribe();

        bus.message(REQUEST).withContent(2).onResponse(counter::incrementAndGet).send();

        await().atMost(200, MILLISECONDS).until(() -> counter.get() == 3);

        bus.unSubscribe(subscriber);
    }

    @Test
    public void testUnSubscribeForMessage() {
        RuntimeException exception = mock(RuntimeException.class);

        Cancellable<MessageType> subscriber1 = bus.subscribingFor(REQUEST).then(() -> {
            throw exception;
        }).subscribe();

        Cancellable<MessageType> subscriber2 = bus.subscribingFor(MessageType.RESPONSE).then(counter::incrementAndGet).subscribe();

        bus.message(REQUEST).withResponse(MessageType.RESPONSE)
              .onError(counter::incrementAndGet).send();

        await().atMost(200, MILLISECONDS).until(() -> counter.get() == 1);
        verify(exception).printStackTrace(out);

        bus.unSubscribe(subscriber2);

        bus.message(REQUEST)
              .withResponse(MessageType.RESPONSE).send();

        await().atMost(200, MILLISECONDS).until(() -> counter.get() == 1);
        verify(exception, times(2)).printStackTrace(out);
        verify(out, times(2)).println(contains(REQUEST.name()));

        bus.unSubscribe(subscriber1);

        subscriber1 = bus.subscribingFor(REQUEST).then(() -> counter.addAndGet(3)).subscribe();

        bus.message(REQUEST)
              .withResponse(MessageType.RESPONSE)
              .send();

        await().atMost(200, MILLISECONDS).until(() -> counter.get() == 4);
        verify(out).println(contains(MessageType.RESPONSE.name()));

        bus.removeAllActionsFor(MessageType.ERROR);

        bus.message(REQUEST)
              .withResponse(MessageType.RESPONSE)
              .send();

        await().atMost(200, MILLISECONDS).until(() -> counter.get() == 7);
        verify(out, times(2)).println(contains(MessageType.RESPONSE.name()));

        bus.unSubscribe(subscriber1);

        bus.message(REQUEST)
              .withResponse(MessageType.RESPONSE)
              .send();

        await().atMost(200, MILLISECONDS).until(
                () -> verify(out, times(3)).println(contains(REQUEST.name())));
    }
}