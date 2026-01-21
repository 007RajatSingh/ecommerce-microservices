package com.Rajat.api_gateway;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
public class FallbackController {
    @RequestMapping("/fallback/order")
    public Mono<String> orderServiceFallback() {
        return Mono.just("Order Service is taking too long to respond or is down. Please try again later.");
    }

    @RequestMapping("/fallback/inventory")
    public Mono<String> inventoryServiceFallback() {
        return Mono.just("Inventory Service is taking too long to respond or is down. Please try again later.");
    }
}
