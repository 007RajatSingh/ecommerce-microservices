package com.Rajat.order_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.Rajat.common.event.OrderCreatedEvent;
import com.Rajat.common.event.OrderItemDto;
import com.Rajat.common.event.OrderPlacedEvent;
import com.Rajat.common.event.PaymentRequestedEvent;
import com.Rajat.common.event.InventoryCompensateEvent;
import com.Rajat.order_service.outbox.OutboxEvent;
import com.Rajat.order_service.outbox.OutboxEventRepository;
import lombok.SneakyThrows;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional // Ensures if one save fails, everything rolls back
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @SneakyThrows
    public String placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setOrderStatus(OrderStatus.PENDING); // Mark as PENDING initially

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();

        order.setOrderLineItemsList(orderLineItems);
        orderRepository.save(order);

        List<OrderItemDto> itemDtos = orderLineItems.stream()
                .map(item -> new OrderItemDto(item.getSkuCode(), item.getQuantity()))
                .toList();
                
        OrderCreatedEvent eventPayload = new OrderCreatedEvent(order.getOrderNumber(), itemDtos);
        
        OutboxEvent outboxEvent = OutboxEvent.builder()
            .aggregateType("Order")
            .aggregateId(order.getOrderNumber())
            .type(OrderCreatedEvent.class.getName())
            .payload(objectMapper.writeValueAsString(eventPayload))
            .createdAt(LocalDateTime.now())
            .processed(false)
            .build();
            
        outboxEventRepository.save(outboxEvent);

        return "Order Placed Successfully, awaiting confirmation";
    }
    
    @SneakyThrows
    public void processInventoryReserved(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber).orElseThrow();
        order.setOrderStatus(OrderStatus.AWAITING_PAYMENT);
        orderRepository.save(order);
        
        // Emit PaymentRequestedEvent
        double totalAmount = order.getOrderLineItemsList().stream()
                .mapToDouble(item -> item.getPrice().doubleValue() * item.getQuantity())
                .sum();
                
        PaymentRequestedEvent eventPayload = new PaymentRequestedEvent(order.getOrderNumber(), totalAmount);
        OutboxEvent outboxEvent = OutboxEvent.builder()
            .aggregateType("Order")
            .aggregateId(order.getOrderNumber())
            .type(PaymentRequestedEvent.class.getName())
            .payload(objectMapper.writeValueAsString(eventPayload))
            .createdAt(LocalDateTime.now())
            .processed(false)
            .build();
            
        outboxEventRepository.save(outboxEvent);
    }
    
    @SneakyThrows
    public void confirmOrder(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber).orElseThrow();
        order.setOrderStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
        
        // Emit final OrderPlacedEvent for notification-service
        OrderPlacedEvent eventPayload = new OrderPlacedEvent(order.getOrderNumber());
        OutboxEvent outboxEvent = OutboxEvent.builder()
            .aggregateType("Order")
            .aggregateId(order.getOrderNumber())
            .type(OrderPlacedEvent.class.getName())
            .payload(objectMapper.writeValueAsString(eventPayload))
            .createdAt(LocalDateTime.now())
            .processed(false)
            .build();
            
        outboxEventRepository.save(outboxEvent);
    }
    
    public void rejectOrder(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber).orElseThrow();
        order.setOrderStatus(OrderStatus.REJECTED);
        orderRepository.save(order);
    }

    @SneakyThrows
    public void cancelOrder(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber).orElseThrow();
        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        
        // Compensate Inventory
        List<OrderItemDto> itemDtos = order.getOrderLineItemsList().stream()
                .map(item -> new OrderItemDto(item.getSkuCode(), item.getQuantity()))
                .toList();
                
        InventoryCompensateEvent eventPayload = new InventoryCompensateEvent(order.getOrderNumber(), itemDtos);
        OutboxEvent outboxEvent = OutboxEvent.builder()
            .aggregateType("Order")
            .aggregateId(order.getOrderNumber())
            .type(InventoryCompensateEvent.class.getName())
            .payload(objectMapper.writeValueAsString(eventPayload))
            .createdAt(LocalDateTime.now())
            .processed(false)
            .build();
            
        outboxEventRepository.save(outboxEvent);
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }
}
