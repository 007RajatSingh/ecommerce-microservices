package com.example.inventory_service;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "t_inventory")
public class Inventory {
    @Id
    @GeneratedValue
    private Long id;

    private String skuCode;
    private Integer quantity;

    @Version
    private Long version;
}
