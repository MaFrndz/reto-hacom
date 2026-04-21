package com.hacom.app_hacom.grpc;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.hacom.app_hacom.akka.OrderProcessorActor;
import com.hacom.app_hacom.akka.ProcessOrderMessage;
import com.hacom.app_hacom.repository.OrderRepository;
import com.hacom.app_hacom.smpp.SmppSenderService;
import io.grpc.stub.StreamObserver;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class OrderGrpcServiceImpl extends OrderServiceGrpc.OrderServiceImplBase {

    private final ActorRef orderProcessorActor;
    private final Counter ordersReceivedCounter;

    public OrderGrpcServiceImpl(ActorSystem actorSystem, OrderRepository orderRepository, SmppSenderService smppSenderService, MeterRegistry meterRegistry) {
        this.orderProcessorActor = actorSystem.actorOf(OrderProcessorActor.props(orderRepository, smppSenderService), "order-processor");
        this.ordersReceivedCounter = meterRegistry.counter("grpc.orders.received.total", "type", "insert_order");
    }

    @Override
    public void insertOrder(InsertOrderRequest request, StreamObserver<InsertOrderResponse> responseObserver) {
        ordersReceivedCounter.increment();
        ProcessOrderMessage msg = new ProcessOrderMessage(request, responseObserver);
        orderProcessorActor.tell(msg, ActorRef.noSender());
    }
}
