package com.Rajat.order_service.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class OutboxScheduler {

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 5000) // Poll every 5 seconds
    @SneakyThrows
    public void processOutboxEvents() {
        List<OutboxEvent> events = outboxEventRepository.findByProcessedFalseOrderByCreatedAtAsc();
        
        for (OutboxEvent event : events) {
            log.info("Processing Outbox Event: {}", event.getId());
            
            // Reconstruct the actual event class from the payload
            Class<?> eventClass = Class.forName(event.getType());
            Object eventPayload = objectMapper.readValue(event.getPayload(), eventClass);
            
            String topic = "notificationTopic";
            if (eventPayload instanceof com.Rajat.common.event.OrderCreatedEvent) {
                topic = "orderTopic";
            } else if (eventPayload instanceof com.Rajat.common.event.PaymentRequestedEvent) {
                topic = "paymentTopic";
            } else if (eventPayload instanceof com.Rajat.common.event.InventoryCompensateEvent) {
                topic = "inventoryTopic";
            }
            
            final String targetTopic = topic;
            
            CompletableFuture.runAsync(() -> {
                kafkaTemplate.send(targetTopic, eventPayload).whenComplete((result, ex) -> {
                    if (ex == null) {
                        event.setProcessed(true);
                        outboxEventRepository.save(event);
                        log.info("Successfully sent event {} to Kafka", event.getId());
                    } else {
                        log.error("Failed to send event {} to Kafka", event.getId(), ex);
                    }
                });
            });
        }
    }
}
