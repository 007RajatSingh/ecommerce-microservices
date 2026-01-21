package com.Rajat.order_service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "inventory-service")
public interface InventoryClient {
    @GetMapping("/api/inventory")
    boolean checkStock(@RequestParam("skuCode") String skuCodeString, @RequestParam("quantity") Integer quantity);
}
