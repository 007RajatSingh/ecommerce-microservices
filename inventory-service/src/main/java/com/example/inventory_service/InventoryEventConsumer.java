package com.example.inventory_service;

import com.Rajat.common.event.InventoryRejectedEvent;
import com.Rajat.common.event.InventoryReservedEvent;
import com.Rajat.common.event.OrderCreatedEvent;
import com.Rajat.common.event.InventoryCompensateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InventoryEventConsumer {

    @Autowired
    private InventroyService inventoryService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "orderTopic", groupId = "inventoryId")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent for order: {}", event.getOrderNumber());
        
        boolean reserved = inventoryService.reserveStock(event.getItems());
        
        if (reserved) {
            log.info("Stock reserved for order: {}", event.getOrderNumber());
            kafkaTemplate.send("inventoryTopic", new InventoryReservedEvent(event.getOrderNumber()));
        } else {
            log.warn("Stock reservation failed for order: {}", event.getOrderNumber());
            kafkaTemplate.send("inventoryTopic", new InventoryRejectedEvent(event.getOrderNumber(), "Insufficient stock"));
        }
    }

    @KafkaListener(topics = "inventoryTopic", groupId = "inventoryId")
    public void handleInventoryCompensate(InventoryCompensateEvent event) {
        log.info("Received InventoryCompensateEvent for order: {}. Releasing stock.", event.getOrderNumber());
        inventoryService.releaseStock(event.getItems());
        log.info("Stock released successfully for order: {}", event.getOrderNumber());
    }
}
