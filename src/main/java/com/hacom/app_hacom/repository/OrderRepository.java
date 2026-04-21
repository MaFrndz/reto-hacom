package com.hacom.app_hacom.repository;

import com.hacom.app_hacom.model.Order;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface OrderRepository extends ReactiveMongoRepository<Order, org.bson.types.ObjectId> {
    Mono<Order> findByOrderId(String orderId);
}
