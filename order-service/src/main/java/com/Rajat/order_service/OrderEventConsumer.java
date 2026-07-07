package com.Rajat.order_service;

import com.Rajat.common.event.InventoryRejectedEvent;
import com.Rajat.common.event.InventoryReservedEvent;
import com.Rajat.common.event.PaymentProcessedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderEventConsumer {

    @Autowired
    private OrderService orderService;

    @KafkaListener(topics = "inventoryTopic", groupId = "orderId")
    public void handleInventoryReserved(InventoryReservedEvent event) {
        log.info("Received InventoryReservedEvent for order: {}", event.getOrderNumber());
        orderService.processInventoryReserved(event.getOrderNumber());
    }

    @KafkaListener(topics = "inventoryTopic", groupId = "orderId")
    public void handleInventoryRejected(InventoryRejectedEvent event) {
        log.info("Received InventoryRejectedEvent for order: {}. Reason: {}", event.getOrderNumber(), event.getReason());
        orderService.rejectOrder(event.getOrderNumber());
    }
    
    @KafkaListener(topics = "paymentResultTopic", groupId = "orderId")
    public void handlePaymentResult(PaymentProcessedEvent event) {
        log.info("Received PaymentProcessedEvent for order: {} with status: {}", event.getOrderNumber(), event.getStatus());
        if ("SUCCESS".equals(event.getStatus())) {
            orderService.confirmOrder(event.getOrderNumber());
        } else {
            orderService.cancelOrder(event.getOrderNumber());
        }
    }
}
