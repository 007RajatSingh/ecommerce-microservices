package com.Rajat.order_service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductClient productClient;

    @Transactional(readOnly = true)
    public CartDto getCart(String userId) {
        Cart cart = getOrCreateCartEntity(userId);
        return mapToDto(cart);
    }

    @Transactional
    public CartDto addItem(String userId, String skuCode, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        Cart cart = getOrCreateCartEntity(userId);

        // Check if item already exists in cart
        Optional<CartItem> existingItemOpt = cart.getCartItems().stream()
                .filter(item -> item.getSkuCode().equals(skuCode))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            // Fetch price from ProductService
            ProductClient.ProductDto product = productClient.getProductBySkuCode(skuCode);
            if (product == null) {
                throw new IllegalArgumentException("Product not found: " + skuCode);
            }

            CartItem newItem = new CartItem();
            newItem.setSkuCode(skuCode);
            newItem.setQuantity(quantity);
            newItem.setUnitPrice(product.getPrice());
            cart.getCartItems().add(newItem);
        }

        recalculateTotal(cart);
        cart = cartRepository.save(cart);
        return mapToDto(cart);
    }

    @Transactional
    public CartDto reduceItem(String userId, String skuCode, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        Cart cart = getOrCreateCartEntity(userId);

        Optional<CartItem> existingItemOpt = cart.getCartItems().stream()
                .filter(item -> item.getSkuCode().equals(skuCode))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            int newQuantity = existingItem.getQuantity() - quantity;
            if (newQuantity <= 0) {
                cart.getCartItems().remove(existingItem);
            } else {
                existingItem.setQuantity(newQuantity);
            }
            recalculateTotal(cart);
            cart = cartRepository.save(cart);
        }
        
        return mapToDto(cart);
    }

    @Transactional
    public void clearCart(String userId) {
        Cart cart = getOrCreateCartEntity(userId);
        cart.getCartItems().clear();
        cart.setTotalCost(BigDecimal.ZERO);
        cartRepository.save(cart);
    }

    public Cart getOrCreateCartEntity(String userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUserId(userId);
            newCart.setCartItems(new ArrayList<>());
            newCart.setTotalCost(BigDecimal.ZERO);
            return cartRepository.save(newCart);
        });
    }

    private void recalculateTotal(Cart cart) {
        BigDecimal total = cart.getCartItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotalCost(total);
    }

    private CartDto mapToDto(Cart cart) {
        CartDto dto = new CartDto();
        dto.setUserId(cart.getUserId());
        dto.setTotalCost(cart.getTotalCost());
        List<CartItemDto> itemDtos = cart.getCartItems().stream()
                .map(item -> new CartItemDto(item.getSkuCode(), item.getQuantity(), item.getUnitPrice()))
                .collect(Collectors.toList());
        dto.setCartItems(itemDtos);
        return dto;
    }
}
