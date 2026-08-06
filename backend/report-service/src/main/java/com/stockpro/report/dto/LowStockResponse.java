package com.stockpro.report.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class LowStockResponse {
    private int totalLowStockProducts;
    private List<LowStockItem> items;
}