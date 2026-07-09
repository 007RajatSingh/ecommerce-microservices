package com.Rajat.order_service;

import org.springframework.stereotype.Component;

@Component
public class ProductClientFallback implements ProductClient {
    @Override
    public ProductDto getProductBySkuCode(String skuCode) {
        throw new RuntimeException("Product Service is down. Cannot verify price for: " + skuCode + ". Order rejected to prevent financial discrepancies.");
    }
}
