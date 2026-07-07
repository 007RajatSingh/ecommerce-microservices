package com.Rajat.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryRejectedEvent {
    private String orderNumber;
    private String reason;
}
