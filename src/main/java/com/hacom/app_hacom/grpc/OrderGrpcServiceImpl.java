package com.hacom.app_hacom.grpc;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.hacom.app_hacom.akka.OrderProcessorActor;
import com.hacom.app_hacom.akka.ProcessOrderMessage;
import com.hacom.app_hacom.repository.OrderRepository;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

@Service
public class OrderGrpcServiceImpl extends OrderServiceGrpc.OrderServiceImplBase {

    private final ActorRef orderProcessorActor;

    public OrderGrpcServiceImpl(ActorSystem actorSystem, OrderRepository orderRepository) {
        this.orderProcessorActor = actorSystem.actorOf(OrderProcessorActor.props(orderRepository), "order-processor");
    }

    @Override
    public void insertOrder(InsertOrderRequest request, StreamObserver<InsertOrderResponse> responseObserver) {
        ProcessOrderMessage msg = new ProcessOrderMessage(request, responseObserver);
        orderProcessorActor.tell(msg, ActorRef.noSender());
    }
}
