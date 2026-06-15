package com.bookflow.auth.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        long accessTokenExpiresIn
) {
}
