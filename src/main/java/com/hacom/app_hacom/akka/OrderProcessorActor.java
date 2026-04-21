package com.hacom.app_hacom.akka;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.hacom.app_hacom.grpc.InsertOrderResponse;
import com.hacom.app_hacom.model.Order;
import com.hacom.app_hacom.repository.OrderRepository;
import com.hacom.app_hacom.smpp.SmppSenderService;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class OrderProcessorActor extends AbstractActor {

    private static final Logger log = LoggerFactory.getLogger(OrderProcessorActor.class);

    private final OrderRepository orderRepository;
    private final SmppSenderService smppSenderService;

    public OrderProcessorActor(OrderRepository orderRepository, SmppSenderService smppSenderService) {
        this.orderRepository = orderRepository;
        this.smppSenderService = smppSenderService;
    }

    public static Props props(OrderRepository orderRepository, SmppSenderService smppSenderService) {
        return Props.create(OrderProcessorActor.class, () -> new OrderProcessorActor(orderRepository, smppSenderService));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ProcessOrderMessage.class, this::processOrder)
                .build();
    }

    private void processOrder(ProcessOrderMessage msg) {
        log.info("Processing order: {}", msg.getRequest().getOrderId());

        Order order = new Order();
        order.set_id(new ObjectId());
        order.setOrderId(msg.getRequest().getOrderId());
        order.setCustomerId(msg.getRequest().getCustomerId());
        order.setCustomerPhoneNumber(msg.getRequest().getCustomerPhoneNumber());
        order.setItems(msg.getRequest().getItemsList());
        order.setStatus("PROCESSING");
        order.setTs(OffsetDateTime.now(ZoneOffset.UTC));

        orderRepository.save(order).subscribe(
                savedDoc -> {
                    log.info("Order saved to MongoDB with ObjectId: {}", savedDoc.get_id());

                    // Aquí llamamos al servicio SMPP para enviar el SMS
                    String message = "Your order " + msg.getRequest().getOrderId() + " has been processed";
                    smppSenderService.sendSms(msg.getRequest().getCustomerPhoneNumber(), message);

                    InsertOrderResponse response = InsertOrderResponse.newBuilder()
                            .setOrderId(savedDoc.getOrderId())
                            .setStatus("PROCESSED_BY_ACTOR")
                            .build();

                    msg.getResponseObserver().onNext(response);
                    msg.getResponseObserver().onCompleted();
                    log.info("Finished processing order: {}", savedDoc.getOrderId());
                },
                error -> {
                    log.error("Error saving order: {}", msg.getRequest().getOrderId(), error);
                    msg.getResponseObserver().onError(error);
                }
        );
    }
}
