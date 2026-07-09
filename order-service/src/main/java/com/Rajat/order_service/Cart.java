package com.Rajat.order_service;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cart {
    @Id
    @GeneratedValue
    private Long id;
    
    @Column(unique = true)
    private String userId; // Associated with the X-Auth-User-Id
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems;
    
    private BigDecimal totalCost = BigDecimal.ZERO;
}
