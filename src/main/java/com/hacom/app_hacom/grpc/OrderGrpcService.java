package com.hacom.app_hacom.grpc;

import com.hacom.app_hacom.model.Order;
import com.hacom.app_hacom.service.OrderService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;

@Component
public class OrderGrpcService extends OrderServiceGrpc.OrderServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(OrderGrpcService.class);

    private final OrderService orderService;

    public OrderGrpcService(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public void insertOrder(InsertOrderRequest request, StreamObserver<InsertOrderResponse> responseObserver) {
        log.info("gRPC InsertOrder called for orderId={}", request.getOrderId());

        Order order = Order.builder()
                .orderId(request.getOrderId())
                .customerId(request.getCustomerId())
                .customerPhoneNumber(request.getCustomerPhoneNumber())
                .items(new ArrayList<>(request.getItemsList()))
                .status("NEW")
                .build();

        orderService.save(order)
                .publishOn(Schedulers.boundedElastic())
                .subscribe(saved -> {
                    InsertOrderResponse resp = InsertOrderResponse.newBuilder()
                            .setOrderId(saved.getOrderId() == null ? "" : saved.getOrderId())
                            .setStatus(saved.getStatus() == null ? "" : saved.getStatus())
                            .build();
                    responseObserver.onNext(resp);
                    responseObserver.onCompleted();
                }, err -> {
                    log.error("Error saving order via gRPC", err);
                    responseObserver.onError(Status.INTERNAL.withDescription(err.getMessage()).asRuntimeException());
                });
    }
}

