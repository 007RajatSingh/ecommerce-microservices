package com.Rajat.order_service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public CartDto getCart(@RequestHeader(value = "X-Auth-User-Id", required = false) String userId) {
        requireUserId(userId);
        return cartService.getCart(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public CartDto addItem(@RequestHeader(value = "X-Auth-User-Id", required = false) String userId,
                           @RequestBody CartItemRequest request) {
        requireUserId(userId);
        return cartService.addItem(userId, request.getSkuCode(), request.getQuantity());
    }

    @PutMapping("/reduce")
    @ResponseStatus(HttpStatus.OK)
    public CartDto reduceItem(@RequestHeader(value = "X-Auth-User-Id", required = false) String userId,
                              @RequestBody CartItemRequest request) {
        requireUserId(userId);
        return cartService.reduceItem(userId, request.getSkuCode(), request.getQuantity());
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    public void clearCart(@RequestHeader(value = "X-Auth-User-Id", required = false) String userId) {
        requireUserId(userId);
        cartService.clearCart(userId);
    }

    private void requireUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new RuntimeException("User must be authenticated");
        }
    }
}

class CartItemRequest {
    private String skuCode;
    private Integer quantity;

    public String getSkuCode() {
        return skuCode;
    }

    public void setSkuCode(String skuCode) {
        this.skuCode = skuCode;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
