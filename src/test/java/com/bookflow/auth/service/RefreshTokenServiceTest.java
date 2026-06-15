package com.bookflow.auth.service;

import java.time.Instant;
import java.util.Optional;

import com.bookflow.auth.entity.RefreshToken;
import com.bookflow.auth.repository.RefreshTokenRepository;
import com.bookflow.common.enums.UserStatus;
import com.bookflow.common.exception.UnauthorizedException;
import com.bookflow.config.JwtProperties;
import com.bookflow.users.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenService refreshTokenService;

    private User testUser;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties("secret", 900_000L, 604_800_000L);
        refreshTokenService = new RefreshTokenService(refreshTokenRepository, props);
        testUser = new User("Test User", "user@example.com", "hash", null, UserStatus.ACTIVE);
    }

    @Test
    void shouldGenerateAndSaveHashedToken() {
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        String rawToken = refreshTokenService.generateAndSave(testUser);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        RefreshToken saved = captor.getValue();

        assertThat(rawToken).isNotBlank();
        assertThat(saved.getTokenHash()).isNotEqualTo(rawToken);
        assertThat(saved.getTokenHash()).hasSize(64); // SHA-256 hex
        assertThat(saved.getUser()).isEqualTo(testUser);
        assertThat(saved.getExpiresAt()).isAfter(Instant.now());
    }

    @Test
    void shouldReturnUserForValidToken() {
        String rawToken = "valid-raw-token";
        RefreshToken token = new RefreshToken(testUser, hashOf(rawToken), Instant.now().plusSeconds(3600));
        when(refreshTokenRepository.findByTokenHash(hashOf(rawToken))).thenReturn(Optional.of(token));

        User result = refreshTokenService.validateAndGetUser(rawToken);

        assertThat(result).isEqualTo(testUser);
    }

    @Test
    void shouldThrowForUnknownToken() {
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.validateAndGetUser("unknown-token"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid refresh token");
    }

    @Test
    void shouldThrowForRevokedToken() {
        String rawToken = "revoked-token";
        RefreshToken token = new RefreshToken(testUser, hashOf(rawToken), Instant.now().plusSeconds(3600));
        token.revoke(Instant.now().minusSeconds(100));
        when(refreshTokenRepository.findByTokenHash(hashOf(rawToken))).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> refreshTokenService.validateAndGetUser(rawToken))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("revoked");
    }

    @Test
    void shouldThrowForExpiredToken() {
        String rawToken = "expired-token";
        RefreshToken token = new RefreshToken(testUser, hashOf(rawToken), Instant.now().minusSeconds(1));
        when(refreshTokenRepository.findByTokenHash(hashOf(rawToken))).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> refreshTokenService.validateAndGetUser(rawToken))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void shouldRevokeActiveToken() {
        String rawToken = "active-token";
        RefreshToken token = new RefreshToken(testUser, hashOf(rawToken), Instant.now().plusSeconds(3600));
        when(refreshTokenRepository.findByTokenHash(hashOf(rawToken))).thenReturn(Optional.of(token));

        refreshTokenService.revoke(rawToken);

        assertThat(token.getRevokedAt()).isNotNull();
    }

    @Test
    void shouldBeIdempotentWhenRevokingAlreadyRevokedToken() {
        String rawToken = "already-revoked";
        Instant originalRevokedAt = Instant.now().minusSeconds(100);
        RefreshToken token = new RefreshToken(testUser, hashOf(rawToken), Instant.now().plusSeconds(3600));
        token.revoke(originalRevokedAt);
        when(refreshTokenRepository.findByTokenHash(hashOf(rawToken))).thenReturn(Optional.of(token));

        refreshTokenService.revoke(rawToken);

        assertThat(token.getRevokedAt()).isEqualTo(originalRevokedAt);
    }

    @Test
    void shouldDoNothingWhenRevokingNonExistentToken() {
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        refreshTokenService.revoke("non-existent-token");

        verify(refreshTokenRepository, never()).save(any());
    }

    private String hashOf(String raw) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
