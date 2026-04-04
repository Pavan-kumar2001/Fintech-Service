package com.fintech.fintech_service.dto.roleandstatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateRoleRequest {

    @NotBlank(message = "Role is required")
    @Pattern(
            regexp = "ADMIN|ANALYST|VIEWER",
            message = "Role must be ADMIN, ANALYST, or VIEWER"
    )
    private String role;
}
