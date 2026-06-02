package com.taskmanager.auth.service;

import com.taskmanager.auth.dto.AuthResponse;
import com.taskmanager.auth.dto.LoginRequest;
import com.taskmanager.auth.dto.RegisterRequest;
import com.taskmanager.common.exception.DuplicateResourceException;
import com.taskmanager.users.entity.User;
import com.taskmanager.users.repository.UserRepository;
import com.devlib.auth.provider.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService — registration and login flows.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthService authService;

    private static final String TEST_EMAIL    = "test@example.com";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "Password123!";
    private static final String HASHED_PW     = "$2a$10$hashedpassword";
    private static final String JWT_TOKEN     = "eyJhbGciOiJIUzI1NiJ9.test.token";

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = User.builder()
                .id("user-001")
                .email(TEST_EMAIL)
                .username(TEST_USERNAME)
                .passwordHash(HASHED_PW)
                .role("ROLE_USER")
                .build();
    }

    // ── register ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("register()")
    class RegisterTests {

        @Test
        @DisplayName("Should register new user and return JWT token")
        void shouldRegisterSuccessfully() {
            RegisterRequest request = new RegisterRequest();
            request.setEmail(TEST_EMAIL);
            request.setUsername(TEST_USERNAME);
            request.setPassword(TEST_PASSWORD);
            request.setFirstName("Test");
            request.setLastName("User");

            when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
            when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);
            when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(HASHED_PW);
            when(userRepository.save(any(User.class))).thenReturn(existingUser);
            when(jwtTokenProvider.generateToken(any(UserDetails.class))).thenReturn(JWT_TOKEN);

            AuthResponse response = authService.register(request);

            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo(JWT_TOKEN);
            assertThat(response.getEmail()).isEqualTo(TEST_EMAIL);
            assertThat(response.getType()).isEqualTo("Bearer");
            assertThat(response.getRole()).isEqualTo("ROLE_USER");

            verify(userRepository).save(argThat(u ->
                u.getEmail().equals(TEST_EMAIL) &&
                u.getPasswordHash().equals(HASHED_PW)
            ));
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException for duplicate email")
        void shouldThrowForDuplicateEmail() {
            RegisterRequest request = new RegisterRequest();
            request.setEmail(TEST_EMAIL);
            request.setUsername("newuser");
            request.setPassword(TEST_PASSWORD);

            when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining(TEST_EMAIL);

            verify(userRepository, never()).save(any());
            verify(jwtTokenProvider, never()).generateToken(any(UserDetails.class));
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException for duplicate username")
        void shouldThrowForDuplicateUsername() {
            RegisterRequest request = new RegisterRequest();
            request.setEmail("new@example.com");
            request.setUsername(TEST_USERNAME);
            request.setPassword(TEST_PASSWORD);

            when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
            when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(true);

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining(TEST_USERNAME);

            verify(userRepository, never()).save(any());
        }
    }

    // ── login ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("login()")
    class LoginTests {

        @Test
        @DisplayName("Should return JWT token on successful login")
        void shouldLoginSuccessfully() {
            LoginRequest request = new LoginRequest();
            request.setEmail(TEST_EMAIL);
            request.setPassword(TEST_PASSWORD);

            UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                    .username(TEST_EMAIL)
                    .password(HASHED_PW)
                    .roles("USER")
                    .build();

            when(userDetailsService.loadUserByUsername(TEST_EMAIL)).thenReturn(userDetails);
            when(passwordEncoder.matches(TEST_PASSWORD, HASHED_PW)).thenReturn(true);
            when(jwtTokenProvider.generateToken(userDetails)).thenReturn(JWT_TOKEN);
            when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(existingUser));

            AuthResponse response = authService.login(request);

            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo(JWT_TOKEN);
            assertThat(response.getEmail()).isEqualTo(TEST_EMAIL);
        }

        @Test
        @DisplayName("Should throw BadCredentialsException for wrong password")
        void shouldThrowForWrongPassword() {
            LoginRequest request = new LoginRequest();
            request.setEmail(TEST_EMAIL);
            request.setPassword("WrongPassword!");

            UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                    .username(TEST_EMAIL)
                    .password(HASHED_PW)
                    .roles("USER")
                    .build();

            when(userDetailsService.loadUserByUsername(TEST_EMAIL)).thenReturn(userDetails);
            when(passwordEncoder.matches("WrongPassword!", HASHED_PW)).thenReturn(false);

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class);

            verify(jwtTokenProvider, never()).generateToken(any(UserDetails.class));
        }
    }
}
