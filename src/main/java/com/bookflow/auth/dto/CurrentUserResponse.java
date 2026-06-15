package com.bookflow.auth.dto;

import java.util.Set;

public record CurrentUserResponse(
        Long id,
        String name,
        String email,
        String phone,
        String status,
        Set<String> roles
) {
}
