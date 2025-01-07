package com.queryapplication.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDTO {
    @NotBlank(message = "email is required")
    private String email;  // Changed from email

    @NotBlank(message = "Password is required")
    private String password;
}
