package com.stockpro.auth.dto;

import com.stockpro.auth.entity.Role;
import lombok.Data;

@Data
public class RegisterRequest {
    private String fullName;
    private String email;
    private String password;
    private String phone;
    private Role role;
    private String adminSecret;
}