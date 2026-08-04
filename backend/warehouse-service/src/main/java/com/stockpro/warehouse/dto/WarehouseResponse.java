package com.stockpro.warehouse.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WarehouseResponse {
    private Long id;
    private String name;
    private String location;
    private Integer capacity;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}