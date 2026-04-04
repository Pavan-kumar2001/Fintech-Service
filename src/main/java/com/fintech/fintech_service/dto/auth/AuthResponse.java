package com.fintech.fintech_service.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private Long userId;
    private String userName;
    private String mobile;
    private String role;

    public AuthResponse(String token, Long userId, String userName, String mobile, String role) {
        this.token = token;
        this.userId = userId;
        this.userName = userName;
        this.mobile = mobile;
        this.role = role;
    }
}