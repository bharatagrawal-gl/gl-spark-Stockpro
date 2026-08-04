package com.stockpro.alert.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AlertResponse {
    private Long id;
    private String alertType;
    private Long referenceId;
    private String referenceType;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;
}