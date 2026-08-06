package com.stockpro.report.dto;

import lombok.Data;

@Data
public class StockLevelDto {
    private Long productId;
    private Long warehouseId;
    private int quantity;
    private int reservedQuantity;
    private int availableQuantity;
}