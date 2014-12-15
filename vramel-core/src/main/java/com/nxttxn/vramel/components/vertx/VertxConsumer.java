package com.nxttxn.vramel.components.vertx;

import com.google.common.base.Optional;
import com.nxttxn.vramel.*;
import com.nxttxn.vramel.impl.DefaultConsumer;
import com.nxttxn.vramel.impl.DefaultExchangeHolder;
import com.nxttxn.vramel.processor.async.AsyncExchangeResult;
import com.nxttxn.vramel.processor.async.OptionalAsyncResultHandler;
import org.apache.commons.lang3.SerializationUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

/**
 * Created with IntelliJ IDEA.
 * User: chuck
 * Date: 6/19/13
 * Time: 11:17 AM
 * To change this template use File | Settings | File Templates.
 */
public class VertxConsumer extends DefaultConsumer {

    private final VertxChannelAdapter endpoint;

    public VertxConsumer(final Endpoint endpoint, final Processor processor) throws Exception {
        super(endpoint, processor);
        this.endpoint = (VertxChannelAdapter) endpoint;

        final VramelContext context = this.endpoint.getVramelContext();
        final Vertx vertx = context.getVertx();

        String address = this.endpoint.getAddress();
        if (vertx.isWorker()) {
            String proxyAddress = address;
            address = String.format("worker-proxied://%s", address);

            context.getContainer().deployVerticle("com.nxttxn.vramel.components.vertx.VertxConsumerWorkerProxy", new JsonObject().putString("proxyAddress", proxyAddress).putString("address", address));
            logger.info("[Vertx Connsumer] Worker detected. Creating Proxy: {} -> {}. See Vertx1.3.1-Final DefaultNetClient.java line 59", proxyAddress, address);
        }


        final String finalAddress = address;
        getEventBus().registerHandler(address, new Handler<Message<byte[]>>() {

            @Override
            public void handle(final Message<byte[]> message) {

                logger.info("[Vertx Consumer] [{}] Received Message", finalAddress);
                Exchange exchange = getEndpoint().createExchange();
                try {
                    DefaultExchangeHolder.unmarshal(exchange, message.body());
                    logger.debug("[Vertx Consumer] Unmarshalled exchange. Exchange transferred.");
                } catch (Exception e) {
                    logger.trace("[Vertx Consumer] Not valid for exchange transfer. Trying VertxMessage.", e);
                    try {
                        final VertxMessage vertxMessage = (VertxMessage) SerializationUtils.deserialize(message.body());
                        exchange.getIn().setBody(vertxMessage.getBody());
                        exchange.getIn().setHeaders(vertxMessage.getHeaders());
                        logger.debug("[Vertx Consumer] VertxMessage processed and new exchange created.");
                    } catch (Exception e1) {
                        exchange.getIn().setBody(message.body());
                        logger.trace("[Vertx Consumer] New exchange created with message body.");
                    }
                }

                try {
                    final Exchange request = exchange;

                    logger.debug("[Vertx Consumer] received message: " + exchange.toString());
                    getAsyncProcessor().process(request, new OptionalAsyncResultHandler() {
                        @Override
                        public void handle(AsyncExchangeResult optionalAsyncResult) {
                            if (optionalAsyncResult.failed()) {
                                request.setException(optionalAsyncResult.getException());
                                sendError(message, request);
                                return;
                            }

                            final Optional<Exchange> result = optionalAsyncResult.result;
                            if (result.isPresent()) {
                                replyWithExchange(message, result.get());
                            } else {
                                replyWithExchange(message, request);
                            }
                        }
                    });
                } catch (Exception e) {
                    logger.error(String.format("[Vertx Consumer] Error processing flow: %s", finalAddress), e);
                    exchange.setException(new RuntimeVramelException("Vertx consumer failed to process message: " + e.getMessage()));
                    sendError(message, exchange);
                }
            }


        });
    }

    private EventBus getEventBus() {
        return endpoint.getVramelContext().getEventBus();
    }


    protected void sendError(Message<byte[]> message, Exchange exchange) {
        logger.error("Error during the exchange processing", exchange.getException());
        replyWithExchange(message, exchange);
    }

    private void replyWithExchange(Message<byte[]> message, Exchange exchange) {

        try {
            DefaultExchangeHolder holder = DefaultExchangeHolder.marshal(exchange);

            message.reply(holder.getBytes());
        } catch (Exception e) {
            logger.error("Unable to marshal the exchange for return", e);
        }
    }
}
