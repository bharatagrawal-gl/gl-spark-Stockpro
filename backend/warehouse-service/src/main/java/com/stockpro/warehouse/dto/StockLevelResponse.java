package com.stockpro.warehouse.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StockLevelResponse {
    private Long id;
    private Long warehouseId;
    private Long productId;
    private Integer quantity;
    private Long version;
    private LocalDateTime updatedAt;
}