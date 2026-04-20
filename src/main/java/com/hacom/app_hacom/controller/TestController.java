package com.hacom.app_hacom.controller;

import com.hacom.app_hacom.model.Order;
import com.hacom.app_hacom.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/")
public class TestController {

    private final OrderService orderService;

    // Constructor injection (no @Autowired)
    public TestController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("test")
    public ResponseEntity<String> test() {

        return ResponseEntity.ok("OK - test endpoint");
    }

    @GetMapping("test/save")
    public Mono<Order> saveTestOrder() {
        // Build a sample Order; OrderService will set _id if null
        Order sample = Order.builder()
                .orderId("ORD-123")
                .customerId("CUST-1")
                .customerPhoneNumber("+51 989091234")
                .status("NEW")
                .items(List.of("item1", "item2"))
                .ts(OffsetDateTime.now())
                .build();

        return orderService.save(sample);
    }
}
