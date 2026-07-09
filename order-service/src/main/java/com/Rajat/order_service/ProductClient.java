package com.Rajat.order_service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@FeignClient(name = "product-service", fallback = ProductClientFallback.class)
public interface ProductClient {

    @GetMapping("/api/products/sku/{skuCode}")
    ProductDto getProductBySkuCode(@PathVariable("skuCode") String skuCode);
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class ProductDto {
        private Long id;
        private String name;
        private BigDecimal price;
    }
}
