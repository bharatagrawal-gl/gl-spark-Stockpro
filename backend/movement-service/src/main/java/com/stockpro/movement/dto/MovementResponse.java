package com.stockpro.movement.dto;

import com.stockpro.movement.entity.MovementType;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MovementResponse {
    private Long id;
    private Long warehouseId;
    private Long productId;
    private MovementType movementType;
    private Integer quantity;
    private Long referenceId;
    private String referenceType;
    private String notes;
    private Long performedBy;
    private LocalDateTime createdAt;
}