package com.bookflow.auth.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.bookflow.auth.dto.AuthResponse;
import com.bookflow.auth.dto.CurrentUserResponse;
import com.bookflow.auth.dto.LoginRequest;
import com.bookflow.auth.dto.LogoutRequest;
import com.bookflow.auth.dto.RefreshTokenRequest;
import com.bookflow.auth.dto.RegisterRequest;
import com.bookflow.auth.mapper.AuthMapper;
import com.bookflow.common.enums.RoleName;
import com.bookflow.common.enums.UserStatus;
import com.bookflow.common.exception.ConflictException;
import com.bookflow.common.exception.UnauthorizedException;
import com.bookflow.users.entity.Role;
import com.bookflow.users.entity.User;
import com.bookflow.users.repository.RoleRepository;
import com.bookflow.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private CustomUserDetailsService userDetailsService;
    @Mock private AuthMapper authMapper;

    private AuthService authService;

    private User activeUser;
    private Role customerRole;
    private UserDetails mockUserDetails;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository, roleRepository, passwordEncoder,
                jwtService, refreshTokenService, userDetailsService, authMapper);

        customerRole = new Role(RoleName.CUSTOMER);
        activeUser = new User("Test User", "user@example.com", "$2a$10$hashed", null, UserStatus.ACTIVE);
        activeUser.addRole(customerRole);

        mockUserDetails = org.springframework.security.core.userdetails.User
                .withUsername("user@example.com").password("$2a$10$hashed").authorities(List.of()).build();
    }

    // --- register ---

    @Test
    void shouldRegisterNewUserSuccessfully() {
        RegisterRequest request = new RegisterRequest("Test User", "user@example.com", "Password1!", null, "CUSTOMER");
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(roleRepository.findByName(RoleName.CUSTOMER)).thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode("Password1!")).thenReturn("$2a$10$hashed");
        when(userRepository.save(any())).thenReturn(activeUser);
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(mockUserDetails);
        when(jwtService.generateAccessToken(mockUserDetails)).thenReturn("access-token");
        when(refreshTokenService.generateAndSave(any())).thenReturn("refresh-token");
        when(jwtService.accessTokenExpirationMs()).thenReturn(900_000L);

        AuthResponse response = authService.register(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.accessTokenExpiresIn()).isEqualTo(900_000L);
        verify(userRepository).save(any());
    }

    @Test
    void shouldThrowConflictWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("Test User", "user@example.com", "Password1!", null, "CUSTOMER");
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email is already in use");

        verify(userRepository, never()).save(any());
    }

    // --- login ---

    @Test
    void shouldLoginSuccessfully() {
        LoginRequest request = new LoginRequest("user@example.com", "Password1!");
        when(userRepository.findActiveByEmailWithRoles("user@example.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("Password1!", activeUser.getPasswordHash())).thenReturn(true);
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(mockUserDetails);
        when(jwtService.generateAccessToken(mockUserDetails)).thenReturn("access-token");
        when(refreshTokenService.generateAndSave(activeUser)).thenReturn("refresh-token");
        when(jwtService.accessTokenExpirationMs()).thenReturn(900_000L);

        AuthResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
    }

    @Test
    void shouldThrowSameErrorForUnknownEmail() {
        LoginRequest request = new LoginRequest("unknown@example.com", "any-password");
        when(userRepository.findActiveByEmailWithRoles("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid email or password.");
    }

    @Test
    void shouldThrowSameErrorForWrongPassword() {
        LoginRequest request = new LoginRequest("user@example.com", "wrong-password");
        when(userRepository.findActiveByEmailWithRoles("user@example.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("wrong-password", activeUser.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid email or password.");
    }

    @Test
    void shouldThrowForDisabledUser() {
        User disabledUser = new User("Disabled", "disabled@example.com", "$2a$10$hash", null, UserStatus.DISABLED);
        LoginRequest request = new LoginRequest("disabled@example.com", "Password1!");
        when(userRepository.findActiveByEmailWithRoles("disabled@example.com")).thenReturn(Optional.of(disabledUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("disabled");
    }

    // --- refreshToken ---

    @Test
    void shouldRotateTokensOnRefresh() {
        RefreshTokenRequest request = new RefreshTokenRequest("old-refresh-token");
        when(refreshTokenService.validateAndGetUser("old-refresh-token")).thenReturn(activeUser);
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(mockUserDetails);
        when(jwtService.generateAccessToken(mockUserDetails)).thenReturn("new-access-token");
        when(refreshTokenService.generateAndSave(activeUser)).thenReturn("new-refresh-token");
        when(jwtService.accessTokenExpirationMs()).thenReturn(900_000L);

        AuthResponse response = authService.refreshToken(request);

        verify(refreshTokenService).revoke("old-refresh-token");
        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
    }

    // --- logout ---

    @Test
    void shouldRevokeTokenOnLogout() {
        LogoutRequest request = new LogoutRequest("some-refresh-token");

        authService.logout(request);

        verify(refreshTokenService).revoke("some-refresh-token");
    }

    // --- getCurrentUser ---

    @Test
    void shouldReturnCurrentUserInfo() {
        CurrentUserResponse expected = new CurrentUserResponse(1L, "Test User", "user@example.com", null, "ACTIVE", Set.of("CUSTOMER"));
        when(userRepository.findActiveByEmailWithRoles("user@example.com")).thenReturn(Optional.of(activeUser));
        when(authMapper.toCurrentUserResponse(activeUser)).thenReturn(expected);

        CurrentUserResponse result = authService.getCurrentUser("user@example.com");

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldThrowWhenCurrentUserNotFound() {
        when(userRepository.findActiveByEmailWithRoles("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getCurrentUser("ghost@example.com"))
                .isInstanceOf(UnauthorizedException.class);
    }

}
