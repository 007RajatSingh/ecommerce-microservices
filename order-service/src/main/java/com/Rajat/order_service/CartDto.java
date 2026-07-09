package com.Rajat.order_service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {
    private String userId;
    private List<CartItemDto> cartItems;
    private BigDecimal totalCost;
}
