package com.stockpro.warehouse.event;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LowStockEvent {
    private Long warehouseId;
    private Long productId;
    private Integer currentQuantity;
    private Integer threshold;
    private LocalDateTime occurredAt;
}