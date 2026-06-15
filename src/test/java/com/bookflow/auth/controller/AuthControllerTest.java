package com.bookflow.auth.controller;

import java.util.Set;

import com.bookflow.auth.dto.AuthResponse;
import com.bookflow.auth.dto.CurrentUserResponse;
import com.bookflow.auth.service.AuthService;
import com.bookflow.auth.service.CustomUserDetailsService;
import com.bookflow.auth.service.JwtService;
import com.bookflow.common.exception.ConflictException;
import com.bookflow.common.exception.GlobalExceptionHandler;
import com.bookflow.common.exception.UnauthorizedException;
import com.bookflow.security.BearerAuthenticationEntryPoint;
import com.bookflow.security.BookFlowAccessDeniedHandler;
import com.bookflow.security.CurrentUserService;
import com.bookflow.security.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class,
        BearerAuthenticationEntryPoint.class, BookFlowAccessDeniedHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private CurrentUserService currentUserService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private static final AuthResponse AUTH_RESPONSE = new AuthResponse("access-token", "refresh-token", 900_000L);

    @Test
    void register_shouldReturn201WithTokens() throws Exception {
        when(authService.register(any())).thenReturn(AUTH_RESPONSE);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Test User","email":"user@example.com","password":"Password1!","role":"CUSTOMER"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));
    }

    @Test
    void register_shouldReturn400ForInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","email":"not-an-email","password":"short","role":"SUPER_ADMIN"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void register_shouldReturn409WhenEmailTaken() throws Exception {
        when(authService.register(any())).thenThrow(new ConflictException("Email is already in use."));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Test","email":"taken@example.com","password":"Password1!","role":"CUSTOMER"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void login_shouldReturn200WithTokens() throws Exception {
        when(authService.login(any())).thenReturn(AUTH_RESPONSE);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@example.com","password":"Password1!"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));
    }

    @Test
    void login_shouldReturn401ForInvalidCredentials() throws Exception {
        when(authService.login(any())).thenThrow(new UnauthorizedException("Invalid email or password."));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@example.com","password":"wrong"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void refreshToken_shouldReturn200WithNewTokens() throws Exception {
        when(authService.refreshToken(any())).thenReturn(AUTH_RESPONSE);

        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"old-refresh-token"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));
    }

    @Test
    void refreshToken_shouldReturn401ForInvalidToken() throws Exception {
        when(authService.refreshToken(any())).thenThrow(new UnauthorizedException("Invalid refresh token."));

        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"bad-token"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_shouldReturn204() throws Exception {
        doNothing().when(authService).logout(any());

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"some-refresh-token"}
                                """))
                .andExpect(status().isNoContent());

        verify(authService).logout(any());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void me_shouldReturn200ForAuthenticatedUser() throws Exception {
        CurrentUserResponse currentUser = new CurrentUserResponse(
                1L, "Test User", "user@example.com", null, "ACTIVE", Set.of("CUSTOMER"));
        when(currentUserService.getAuthenticatedEmail()).thenReturn("user@example.com");
        when(authService.getCurrentUser("user@example.com")).thenReturn(currentUser);

        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("user@example.com"))
                .andExpect(jsonPath("$.data.name").value("Test User"));
    }

    @Test
    void me_shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));
    }

    @Test
    void shouldReturn401WithJsonBodyNotHtml() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

}
