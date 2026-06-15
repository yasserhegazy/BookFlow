package com.bookflow.auth.service;

import java.util.List;

import com.bookflow.config.JwtProperties;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "bookflow-test-secret-key-that-is-at-least-256-bits-long-for-hs256";
    private static final long ACCESS_EXPIRATION_MS = 900_000L;
    private static final long REFRESH_EXPIRATION_MS = 604_800_000L;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties(SECRET, ACCESS_EXPIRATION_MS, REFRESH_EXPIRATION_MS);
        jwtService = new JwtService(props);
    }

    @Test
    void shouldGenerateTokenWithCorrectSubject() {
        UserDetails userDetails = buildUserDetails("user@example.com");

        String token = jwtService.generateAccessToken(userDetails);

        assertThat(jwtService.extractEmail(token)).isEqualTo("user@example.com");
    }

    @Test
    void shouldReturnTrueForValidToken() {
        UserDetails userDetails = buildUserDetails("user@example.com");
        String token = jwtService.generateAccessToken(userDetails);

        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void shouldReturnFalseForTokenWithDifferentEmail() {
        UserDetails owner = buildUserDetails("owner@example.com");
        UserDetails other = buildUserDetails("other@example.com");
        String token = jwtService.generateAccessToken(owner);

        assertThat(jwtService.isTokenValid(token, other)).isFalse();
    }

    @Test
    void shouldReturnFalseForExpiredToken() {
        JwtProperties shortLivedProps = new JwtProperties(SECRET, -1L, REFRESH_EXPIRATION_MS);
        JwtService shortLivedService = new JwtService(shortLivedProps);
        UserDetails userDetails = buildUserDetails("user@example.com");

        String token = shortLivedService.generateAccessToken(userDetails);

        assertThat(shortLivedService.isTokenValid(token, userDetails)).isFalse();
    }

    @Test
    void shouldReturnFalseForTamperedToken() {
        UserDetails userDetails = buildUserDetails("user@example.com");
        String token = jwtService.generateAccessToken(userDetails) + "tampered";

        assertThat(jwtService.isTokenValid(token, userDetails)).isFalse();
    }

    @Test
    void shouldExposeCorrectAccessTokenExpirationMs() {
        assertThat(jwtService.accessTokenExpirationMs()).isEqualTo(ACCESS_EXPIRATION_MS);
    }

    @Test
    void shouldThrowForMalformedToken() {
        assertThatThrownBy(() -> jwtService.extractEmail("not.a.valid.jwt.token"))
                .isInstanceOf(JwtException.class);
    }

    private UserDetails buildUserDetails(String email) {
        return User.withUsername(email).password("irrelevant").authorities(List.of()).build();
    }

}
