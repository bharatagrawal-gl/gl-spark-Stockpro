package com.stockpro.alert.event;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PoOverdueEvent {
    private Long purchaseOrderId;
    private Long supplierId;
    private LocalDate expectedDate;
    private LocalDateTime occurredAt;
}