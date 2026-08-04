package com.stockpro.alert.event;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class LowStockEvent {
    private Long warehouseId;
    private Long productId;
    private Integer currentQuantity;
    private Integer threshold;
    private LocalDateTime occurredAt;
}