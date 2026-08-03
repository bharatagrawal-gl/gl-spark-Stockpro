package com.stockpro.product.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductRequest {
    private String name;
    private String sku;
    private BigDecimal price;
    private int reorderLevel;
    private String barcode;
}