package com.hacom.app_hacom.repository;

import com.hacom.app_hacom.model.Order;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface OrderRepository extends ReactiveMongoRepository<Order, ObjectId> {
    Mono<Order> findByOrderId(String orderId);
}
