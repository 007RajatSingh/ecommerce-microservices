package com.Rajat.order_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String placeOrder(@RequestBody OrderRequest orderRequest) {
        return orderService.placeOrder(orderRequest);
    }

    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public String checkoutCart(@RequestHeader(value = "X-Auth-User-Id", required = false) String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new RuntimeException("User must be authenticated");
        }
        return orderService.checkoutCart(userId);
    }
}
