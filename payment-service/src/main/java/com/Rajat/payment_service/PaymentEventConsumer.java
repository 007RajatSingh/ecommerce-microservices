package com.Rajat.payment_service;

import com.Rajat.common.event.PaymentProcessedEvent;
import com.Rajat.common.event.PaymentRequestedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@Slf4j
public class PaymentEventConsumer {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    private final Random random = new Random();

    @KafkaListener(topics = "paymentTopic", groupId = "paymentId")
    public void handlePaymentRequested(PaymentRequestedEvent event) {
        log.info("Received PaymentRequestedEvent for order: {} with amount: {}", event.getOrderNumber(), event.getAmount());
        
        // Mock payment processing (10% chance to fail)
        boolean success = random.nextInt(10) != 0;
        
        String status = success ? "SUCCESS" : "FAILED";
        log.info("Payment for order {} processed with status: {}", event.getOrderNumber(), status);
        
        kafkaTemplate.send("paymentResultTopic", new PaymentProcessedEvent(event.getOrderNumber(), status));
    }
}
