package com.stockpro.warehouse.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StockUpdateRequest {
    private Long warehouseId;
    private Long productId;
    private Integer quantity;  // positive = add, negative = deduct
}