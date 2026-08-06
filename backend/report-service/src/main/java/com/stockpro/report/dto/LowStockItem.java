package com.stockpro.report.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LowStockItem {
    private Long productId;
    private String productName;
    private String sku;
    private int currentQuantity;
    private int reorderLevel;
    private int shortage;
}