package com.stockpro.purchase.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PurchaseOrderItemRequest {
    private Long productId;
    private Long warehouseId;
    private Integer quantityOrdered;
    private BigDecimal unitPrice;
}