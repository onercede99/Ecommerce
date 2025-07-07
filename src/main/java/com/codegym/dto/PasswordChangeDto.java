package com.codegym.dto;

import lombok.Data;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class PasswordChangeDto {

    @NotEmpty(message = "Current password cannot be empty")
    private String currentPassword;

    @NotEmpty(message = "New password cannot be empty")
    @Size(min = 6, message = "New password must have at least 6 characters")
    private String newPassword;

    @NotEmpty(message = "Confirm password cannot be empty")
    private String confirmPassword;
}