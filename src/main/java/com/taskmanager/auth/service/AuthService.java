package com.taskmanager.auth.service;

import com.devlib.auth.provider.JwtTokenProvider;
import com.taskmanager.auth.dto.AuthResponse;
import com.taskmanager.auth.dto.LoginRequest;
import com.taskmanager.auth.dto.RegisterRequest;
import com.taskmanager.common.exception.DuplicateResourceException;
import com.taskmanager.users.entity.User;
import com.taskmanager.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Authentication service handling registration and login.
 *
 * JWT token generation is delegated entirely to JwtTokenProvider
 * from the jwt-auth-starter — no custom JWT logic here.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;       // auto-wired from jwt-auth-starter
    private final JwtTokenProvider jwtTokenProvider;    // auto-wired from jwt-auth-starter
    private final UserDetailsService userDetailsService; // our UserDetailsServiceImpl

    private static final String DEFAULT_ROLE = "ROLE_USER";
    private static final long TOKEN_EXPIRY_MS = 86_400_000L;

    // ── Register ──────────────────────────────────────────────────────────

    /**
     * Registers a new user, hashes their password, and returns a JWT token.
     */
    public AuthResponse register(RegisterRequest request) {
        // Validate uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "An account with email '" + request.getEmail() + "' already exists");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException(
                    "Username '" + request.getUsername() + "' is already taken");
        }

        // Build and persist user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(DEFAULT_ROLE)
                .build();

        userRepository.save(user);
        log.info("New user registered: {} ({})", user.getUsername(), user.getEmail());

        // Generate token using starter's JwtTokenProvider
        UserDetails userDetails = buildUserDetails(user);
        String token = jwtTokenProvider.generateToken(userDetails);

        return buildAuthResponse(token, user);
    }

    // ── Login ─────────────────────────────────────────────────────────────

    /**
     * Authenticates a user and returns a JWT token.
     */
    public AuthResponse login(LoginRequest request) {
        // Load user from DB
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());

        // Validate password against BCrypt hash
        if (!passwordEncoder.matches(request.getPassword(), userDetails.getPassword())) {
            log.warn("Failed login attempt for email: {}", request.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }

        // Generate token using starter's JwtTokenProvider
        String token = jwtTokenProvider.generateToken(userDetails);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BadCredentialsException("User not found"));

        log.info("User logged in: {}", request.getEmail());
        return buildAuthResponse(token, user);
    }

    // ── Private Helpers ───────────────────────────────────────────────────

    private UserDetails buildUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority(user.getRole())))
                .build();
    }

    private AuthResponse buildAuthResponse(String token, User user) {
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole())
                .expiresIn(TOKEN_EXPIRY_MS)
                .build();
    }
}
