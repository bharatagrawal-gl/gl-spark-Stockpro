package com.stockpro.report.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class StockValueResponse {
    private BigDecimal totalStockValue;
    private int totalProducts;
    private int totalUnits;
    private List<ProductStockValue> breakdown;
}