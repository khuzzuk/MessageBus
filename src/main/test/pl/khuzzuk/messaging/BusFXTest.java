package pl.khuzzuk.messaging;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.PrintStream;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;

//TODO test with testFX in order to actually test javafx thread interaction
public class BusFXTest {

    private Bus<MessageType> bus;
    private Label label;
    private String initialValue;
    private String expected;

    @BeforeClass
    public void setUp() throws Exception {
        initialValue = "1";
        expected = "2";
        new Thread(() -> Application.launch(TestApplication.class)).start();
        bus = Bus.initializeBus(MessageType.class, false);
    }

    @BeforeMethod
    public void beforeTests() throws Exception {
        label = new Label(initialValue);
    }

    @AfterMethod
    public void tearDown() throws Exception {
    }

    @AfterClass
    public void closeTestsApp() throws Exception {
        bus.closeBus();
        Platform.exit();
    }

    @Test
    public void publishSubscribeCheck() throws Exception {
        Object subscriber = bus.setReaction(MessageType.MESSAGE, () -> label.setText(expected));
        bus.send(MessageType.MESSAGE);
        label.setVisible(true);

        await().pollDelay(50, MILLISECONDS).atMost(1000, MILLISECONDS).until(() -> label.getText().equals(expected));

        bus.unSubscribe(subscriber);
    }

    @Test
    public void publishContent() {
        Object subscriber = bus.setReaction(MessageType.MESSAGE, label::setText);
        bus.send(MessageType.MESSAGE, expected);

        await().atMost(300, MILLISECONDS).until(() -> label.getText().equals(expected));

        bus.unSubscribe(subscriber);
    }

    @Test
    public void requestCheck() throws Exception {
        Object subscriber1 = bus.setResponse(MessageType.REQUEST, () -> {});
        Object subscriber2 = bus.setReaction(MessageType.RESPONSE, () -> label.setText(expected));
        bus.sendMessage(MessageType.REQUEST, MessageType.RESPONSE);

        await().atMost(300, MILLISECONDS).until(() -> label.getText().equals(expected));

        bus.unSubscribe(subscriber1);
        bus.unSubscribe(subscriber2);
    }

    @Test
    public void requestBagTest() throws Exception {
        Object subscriber1 = bus.setResponse(MessageType.REQUEST, () -> label.setText(expected));
        Object subscriber2 = bus.setReaction(MessageType.RESPONSE, () -> label.setText(label.getText() + expected));
        bus.send(MessageType.REQUEST, MessageType.RESPONSE, expected);

        await().atMost(300, MILLISECONDS).until(() -> label.getText().equals(expected + expected));

        bus.unSubscribe(subscriber1);
        bus.unSubscribe(subscriber2);
    }

    @Test
    public void describeExceptionWhenTryingToSendSimpleMessageForRequest() throws Exception {
        PrintStream mocked = Mockito.mock(PrintStream.class);
        System.setErr(mocked);

        Object subscriber = bus.setFXResponse(MessageType.MESSAGE, () -> {});
        bus.send(MessageType.MESSAGE);

        await().atMost(300, MILLISECONDS).until(() -> verify(mocked).println(contains(MessageType.MESSAGE.name())));

        bus.unSubscribe(subscriber);
    }

    @Test
    public void errorResponse() throws Exception {
        Object subscriber1 = bus.setFXResponse(MessageType.REQUEST, () -> {
            throw new IllegalStateException();
        });
        Object subscriber2 = bus.setFXReaction(MessageType.ERROR, () -> label.setText(label.getText() + expected));
        Object subscriber3 = bus.setFXReaction(MessageType.RESPONSE, () -> label.setText(label.getText() + "unexpected"));

        bus.sendMessage(MessageType.REQUEST, MessageType.RESPONSE, MessageType.ERROR);

        await().atMost(300, MILLISECONDS).until(() -> {
            String result = label.getText();
            return result.contains(initialValue) && result.contains(expected);
        });

        bus.unSubscribe(subscriber1);
        bus.unSubscribe(subscriber2);
        bus.unSubscribe(subscriber3);
    }

    @Test
    public void errorResponseWithContent() throws Exception {
        Object subscriber1 = bus.setFXResponse(MessageType.REQUEST, __ -> {
            throw new IllegalStateException();
        });
        Object subscriber2 = bus.setFXReaction(MessageType.ERROR, () -> label.setText(expected));
        Object subscriber3 = bus.setFXReaction(MessageType.RESPONSE, () -> label.setText("unexpected"));

        bus.send(MessageType.REQUEST, MessageType.RESPONSE, 1, MessageType.ERROR);

        await().atMost(300, MILLISECONDS).until(() -> label.getText().equals(expected));

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
        Object subscriber2 = bus.setReaction(MessageType.RESPONSE, () -> label.setText("unexpected"));

        bus.sendMessage(MessageType.REQUEST, MessageType.RESPONSE);

        await().atMost(300, MILLISECONDS).until(() -> verify(mocked).printStackTrace());

        bus.unSubscribe(subscriber1);
        bus.unSubscribe(subscriber2);
    }

    @Test
    public void errorResponseWithContentWithoutErrorTopic() throws Exception {
        RuntimeException mocked = Mockito.mock(RuntimeException.class);

        Object subscriber1 = bus.setResponse(MessageType.REQUEST, __ -> {
            throw mocked;
        });
        Object subscriber2 = bus.setReaction(MessageType.RESPONSE, () -> label.setText("unexpected"));

        bus.send(MessageType.REQUEST, MessageType.RESPONSE, "unexpected");

        await().atMost(300, MILLISECONDS).until(() -> verify(mocked).printStackTrace());
        Assert.assertEquals(label.getText(), initialValue);

        bus.unSubscribe(subscriber1);
        bus.unSubscribe(subscriber2);
    }

    public static class TestApplication extends Application {
        @Override
        public void start(Stage primaryStage) throws Exception {
        }
    }
}