package com.stockpro.supplier.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SupplierResponse {
    private Long id;
    private String name;
    private String contactName;
    private String email;
    private String phone;
    private String address;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}