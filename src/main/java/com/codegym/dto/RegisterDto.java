package com.codegym.dto;

import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class RegisterDto {
    @NotEmpty(message = "Username should not be empty")
    private String username;

    @NotEmpty(message = "Email should not be empty")
    @Email(message = "Email should be a valid email format")
    private String email;

    @NotEmpty(message = "Password should not be empty")
    @Size(min = 6, message = "Password should have at least 6 characters")
    private String password;
}
