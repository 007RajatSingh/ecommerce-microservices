package com.Rajat.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryCompensateEvent {
    private String orderNumber;
    private List<OrderItemDto> items;
}
