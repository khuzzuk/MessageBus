package pl.khuzzuk.messaging;

import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Log4j2
class MultiRequestBagSubscriber extends AbstractMultiContentSubscriber<RequestBagMessage<Object>> implements MultiRequestContentSubscriber {
    private Map<String, List<Function>> responseResolvers;

    @Override
    public void subscribe(String msgType, Function responseResolver) {
        assureInit();
        responseResolvers.computeIfAbsent(msgType, k -> new ArrayList<>());
        responseResolvers.get(msgType).add(responseResolver);
        getBus().subscribe(this, msgType);
    }

    @Override
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
                    .publish(new ContentMessage().setType(message.getResponseType()).setMessage(r.apply(message.getMessage()))));
        } catch (ClassCastException e) {
            log.error("Wrong type of Message for Consumer: " + responseResolvers.get(message.getType()) +
                    " and message type: " + message.getType());
            e.printStackTrace();
        }
    }

    @Override
    void assureInit() {
        super.assureInit();
        if (responseResolvers == null) {
            responseResolvers = new HashMap<>();
        }
    }
}
