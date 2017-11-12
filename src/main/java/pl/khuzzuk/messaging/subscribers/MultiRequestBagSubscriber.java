package pl.khuzzuk.messaging.subscribers;

import org.apache.logging.log4j.Logger;
import pl.khuzzuk.messaging.messages.RequestBagMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class MultiRequestBagSubscriber extends AbstractMultiContentSubscriber<RequestBagMessage<Object>> implements MultiRequestContentSubscriber {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(MultiRequestBagSubscriber.class);
    private Map<String, List<Function>> responseResolvers;

    @Override
    public void subscribe(String msgType, Function responseResolver) {
        assureInit();
        responseResolvers.computeIfAbsent(msgType, k -> new ArrayList<>());
        responseResolvers.get(msgType).add(responseResolver);
        getBus().subscribe(this, msgType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public RequestContentSubscriber setResponseResolver(Function responseResolver) {
        responseResolvers.computeIfAbsent(getMessageType(), k -> new ArrayList<>());
        responseResolvers.get(getMessageType()).add(responseResolver);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void receive(RequestBagMessage message) {
        try {
            super.receive(message);
            responseResolvers.get(message.getType()).forEach(r -> getBus()
                    .send(message.getResponseType(), r.apply(message.getMessage())));
        } catch (ClassCastException e) {
            log.error("Wrong type of Message for Consumer: " + responseResolvers.get(message.getType()) +
                    " and message type: " + message.getType());
            e.printStackTrace();
        }
    }

    @Override
    public void receive(Object content) {
        throw new UnsupportedOperationException();
    }

    @Override
    void assureInit() {
        super.assureInit();
        if (responseResolvers == null) {
            responseResolvers = new HashMap<>();
        }
    }
}
