package com.Rajat.order_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;

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
    private CartService cartService;

    @Autowired
    private ProductClient productClient;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @SneakyThrows
    public String placeOrder(OrderRequest orderRequest) {
        if (orderRequest.getOrderLineItemsDtoList() == null || orderRequest.getOrderLineItemsDtoList().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setOrderStatus(OrderStatus.PENDING); // Mark as PENDING initially

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(dto -> {
                    if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
                        throw new IllegalArgumentException("Quantity must be greater than zero");
                    }
                    ProductClient.ProductDto product = productClient.getProductBySkuCode(dto.getSkuCode());
                    if (product == null) {
                        throw new IllegalArgumentException("Product not found: " + dto.getSkuCode());
                    }
                    OrderLineItems item = new OrderLineItems();
                    item.setSkuCode(dto.getSkuCode());
                    item.setQuantity(dto.getQuantity());
                    item.setPrice(product.getPrice()); // Use actual price from ProductService
                    return item;
                })
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
    public String checkoutCart(String userId) {
        CartDto cart = cartService.getCart(userId);
        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setOrderStatus(OrderStatus.PENDING); // Mark as PENDING initially

        List<OrderLineItems> orderLineItems = cart.getCartItems().stream()
                .map(itemDto -> {
                    OrderLineItems item = new OrderLineItems();
                    item.setSkuCode(itemDto.getSkuCode());
                    item.setQuantity(itemDto.getQuantity());
                    item.setPrice(itemDto.getUnitPrice());
                    return item;
                })
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

        cartService.clearCart(userId);

        return "Order Placed Successfully from Cart, awaiting confirmation";
    }
    
    @SneakyThrows
    public void processInventoryReserved(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber).orElseThrow();
        order.setOrderStatus(OrderStatus.AWAITING_PAYMENT);
        orderRepository.save(order);
        
        // Emit PaymentRequestedEvent
        BigDecimal totalAmount = order.getOrderLineItemsList().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
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

}
