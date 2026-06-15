package com.bookflow.auth.service;

import com.bookflow.auth.dto.AuthResponse;
import com.bookflow.auth.dto.CurrentUserResponse;
import com.bookflow.auth.dto.LoginRequest;
import com.bookflow.auth.dto.LogoutRequest;
import com.bookflow.auth.dto.RefreshTokenRequest;
import com.bookflow.auth.dto.RegisterRequest;
import com.bookflow.auth.mapper.AuthMapper;
import com.bookflow.common.enums.RoleName;
import com.bookflow.common.enums.UserStatus;
import com.bookflow.common.exception.BadRequestException;
import com.bookflow.common.exception.ConflictException;
import com.bookflow.common.exception.UnauthorizedException;
import com.bookflow.users.entity.Role;
import com.bookflow.users.entity.User;
import com.bookflow.users.repository.RoleRepository;
import com.bookflow.users.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final CustomUserDetailsService userDetailsService;
    private final AuthMapper authMapper;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService,
                       CustomUserDetailsService userDetailsService,
                       AuthMapper authMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.userDetailsService = userDetailsService;
        this.authMapper = authMapper;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email is already in use.");
        }

        RoleName roleName = RoleName.valueOf(request.role());
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new BadRequestException("Role not found: " + request.role()));

        String passwordHash = passwordEncoder.encode(request.password());
        User user = new User(request.name(), request.email(), passwordHash, request.phone(), UserStatus.ACTIVE);
        user.addRole(role);
        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateAccessToken(userDetails);
        String rawRefreshToken = refreshTokenService.generateAndSave(user);

        return new AuthResponse(accessToken, rawRefreshToken, jwtService.accessTokenExpirationMs());
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findActiveByEmailWithRoles(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password."));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password.");
        }

        if (user.getStatus() == UserStatus.DISABLED) {
            throw new UnauthorizedException("Account is disabled.");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateAccessToken(userDetails);
        String rawRefreshToken = refreshTokenService.generateAndSave(user);

        return new AuthResponse(accessToken, rawRefreshToken, jwtService.accessTokenExpirationMs());
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        User user = refreshTokenService.validateAndGetUser(request.refreshToken());
        refreshTokenService.revoke(request.refreshToken());

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateAccessToken(userDetails);
        String newRawRefreshToken = refreshTokenService.generateAndSave(user);

        return new AuthResponse(accessToken, newRawRefreshToken, jwtService.accessTokenExpirationMs());
    }

    @Transactional
    public void logout(LogoutRequest request) {
        refreshTokenService.revoke(request.refreshToken());
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse getCurrentUser(String email) {
        User user = userRepository.findActiveByEmailWithRoles(email)
                .orElseThrow(() -> new UnauthorizedException("User not found."));
        return authMapper.toCurrentUserResponse(user);
    }

}
