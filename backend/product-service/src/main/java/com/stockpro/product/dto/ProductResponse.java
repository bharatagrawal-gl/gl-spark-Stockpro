package com.stockpro.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String sku;
    private BigDecimal price;
    private int reorderLevel;
    private String barcode;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}