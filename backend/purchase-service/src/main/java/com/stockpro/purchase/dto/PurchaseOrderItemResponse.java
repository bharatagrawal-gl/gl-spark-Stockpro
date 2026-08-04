package com.stockpro.purchase.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PurchaseOrderItemResponse {
    private Long id;
    private Long productId;
    private Long warehouseId;
    private Integer quantityOrdered;
    private Integer quantityReceived;
    private BigDecimal unitPrice;
    private LocalDateTime createdAt;
}