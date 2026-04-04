package com.fintech.fintech_service.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AuthRequest {

    @NotBlank(message = "mobile number is required")
    private String mobile;

    @NotBlank(message = "Password is required")
    private String password;
}
