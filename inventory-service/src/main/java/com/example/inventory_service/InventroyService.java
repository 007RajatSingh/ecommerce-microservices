package com.example.inventory_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.Rajat.common.event.OrderItemDto;
import java.util.List;
import java.util.Optional;

@Service
public class InventroyService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public boolean isInStock(String skuCode, Integer quantity) {
        if (quantity == null || quantity <= 0) return false;
        
        return inventoryRepository.findBySkuCode(skuCode)
                .map(inventory -> inventory.getQuantity() >= quantity)
                .orElse(false); // count as out of stock
    }

    @Transactional
    public boolean reserveStock(List<OrderItemDto> items) {
        // check all first
        for (OrderItemDto item : items) {
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                return false; // invalid quantity
            }
            Optional<Inventory> opt = inventoryRepository.findBySkuCode(item.getSkuCode());
            if (opt.isEmpty() || opt.get().getQuantity() < item.getQuantity()) {
                return false; // not enough stock
            }
        }
        
        // deduct stock
        for (OrderItemDto item : items) {
            Inventory inv = inventoryRepository.findBySkuCode(item.getSkuCode()).get();
            inv.setQuantity(inv.getQuantity() - item.getQuantity());
            inventoryRepository.save(inv);
        }
        return true;
    }

    @Transactional
    public void releaseStock(List<OrderItemDto> items) {
        for (OrderItemDto item : items) {
            Optional<Inventory> opt = inventoryRepository.findBySkuCode(item.getSkuCode());
            if (opt.isPresent()) {
                Inventory inv = opt.get();
                inv.setQuantity(inv.getQuantity() + item.getQuantity());
                inventoryRepository.save(inv);
            }
        }
    }

    public void createInventory(Inventory inventory) {
        inventoryRepository.save(inventory);
    }

}
