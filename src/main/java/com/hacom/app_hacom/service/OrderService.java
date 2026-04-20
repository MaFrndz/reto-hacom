package com.hacom.app_hacom.service;

import com.hacom.app_hacom.model.Order;
import com.hacom.app_hacom.repository.OrderRepository;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OrderService {

    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    public Mono<Order> save(Order order) {
        if (order.get_id() == null) {
            order.set_id(new ObjectId());
        }
        return repository.save(order);
    }

    public Mono<Order> findByOrderId(String orderId) {
        return repository.findByOrderId(orderId);
    }

    public Flux<Order> findAll() {
        return repository.findAll();
    }

    public Mono<Void> deleteByOrderId(String orderId) {
        return repository.findByOrderId(orderId)
                .flatMap(o -> repository.deleteById(o.get_id()));
    }
}
