package com.stockpro.auth.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ValidateResponse {
    private boolean valid;
    private String message;
    private String email;   // ← ADD
    private String role;    // ← ADD
    private Long userId;    // ← ADD
}