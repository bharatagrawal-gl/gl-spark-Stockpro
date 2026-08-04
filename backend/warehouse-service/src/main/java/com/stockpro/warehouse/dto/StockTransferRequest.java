package com.stockpro.warehouse.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StockTransferRequest {
    private Long fromWarehouseId;
    private Long toWarehouseId;
    private Long productId;
    private Integer quantity;
}