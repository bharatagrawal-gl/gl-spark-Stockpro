package com.stockpro.report.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductDto {
    private Long id;
    private String name;
    private String sku;
    private BigDecimal price;
    private int reorderLevel;
    private boolean isActive;
}