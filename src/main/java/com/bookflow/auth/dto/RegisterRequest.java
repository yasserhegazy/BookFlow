package com.bookflow.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 150, message = "Name must not exceed 150 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        String password,

        @Size(max = 50, message = "Phone must not exceed 50 characters")
        String phone,

        @NotBlank(message = "Role is required")
        @Pattern(regexp = "BUSINESS_OWNER|STAFF|CUSTOMER", message = "Role must be BUSINESS_OWNER, STAFF, or CUSTOMER")
        String role
) {
}
