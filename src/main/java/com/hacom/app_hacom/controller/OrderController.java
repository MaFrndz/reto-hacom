package com.hacom.app_hacom.controller;

import com.hacom.app_hacom.model.Order;
import com.hacom.app_hacom.repository.OrderRepository;
import com.hacom.app_hacom.model.OrdersCountResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orderRepository;

    public OrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping("/{orderId}")
    public Mono<ResponseEntity<Order>> getOrderByOrderId(@PathVariable String orderId) {
        return orderRepository.findByOrderId(orderId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/count")
    public Mono<OrdersCountResponse> getOrdersCountByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end) {
        return orderRepository.findByTsBetween(start, end)
                .count()
                .map(OrdersCountResponse::new);
    }
}
