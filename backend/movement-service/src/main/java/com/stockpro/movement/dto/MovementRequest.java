package com.stockpro.movement.dto;

import com.stockpro.movement.entity.MovementType;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MovementRequest {
    private Long warehouseId;
    private Long productId;
    private MovementType movementType;
    private Integer quantity;
    private Long referenceId;
    private String referenceType;
    private String notes;
    private Long performedBy;
}