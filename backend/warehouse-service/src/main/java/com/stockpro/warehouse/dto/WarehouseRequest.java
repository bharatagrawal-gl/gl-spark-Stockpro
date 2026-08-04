package com.stockpro.warehouse.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WarehouseRequest {
    private String name;
    private String location;
    private Integer capacity;
}