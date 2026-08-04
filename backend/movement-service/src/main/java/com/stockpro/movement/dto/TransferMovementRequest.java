package com.stockpro.movement.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TransferMovementRequest {
    private Long fromWarehouseId;
    private Long toWarehouseId;
    private Long productId;
    private Integer quantity;
    private Long referenceId;
    private String notes;
    private Long performedBy;
}