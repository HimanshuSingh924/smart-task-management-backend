package com.taskmanager.users.service;

import com.taskmanager.common.exception.DuplicateResourceException;
import com.taskmanager.common.exception.ResourceNotFoundException;
import com.taskmanager.common.mapper.UserMapper;
import com.taskmanager.common.util.SecurityUtils;
import com.taskmanager.users.dto.UpdateProfileRequest;
import com.taskmanager.users.dto.UserProfileResponse;
import com.taskmanager.users.entity.User;
import com.taskmanager.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService — profile read and update operations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private SecurityUtils securityUtils;

    @InjectMocks
    private UserService userService;

    private static final String USER_EMAIL = "test@example.com";

    private User testUser;
    private UserProfileResponse testProfileResponse;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id("user-001")
                .email(USER_EMAIL)
                .username("testuser")
                .firstName("Test")
                .lastName("User")
                .role("ROLE_USER")
                .build();

        testProfileResponse = UserProfileResponse.builder()
                .id("user-001")
                .email(USER_EMAIL)
                .username("testuser")
                .firstName("Test")
                .lastName("User")
                .role("ROLE_USER")
                .build();
    }

    @Nested
    @DisplayName("getCurrentUserProfile()")
    class GetProfileTests {

        @Test
        @DisplayName("Should return profile for authenticated user")
        void shouldReturnProfile() {
            when(securityUtils.getCurrentUserEmail()).thenReturn(USER_EMAIL);
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(testUser));
            when(userMapper.toProfileResponse(testUser)).thenReturn(testProfileResponse);

            UserProfileResponse result = userService.getCurrentUserProfile();

            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(USER_EMAIL);
            assertThat(result.getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user not in DB")
        void shouldThrowWhenUserNotFound() {
            when(securityUtils.getCurrentUserEmail()).thenReturn(USER_EMAIL);
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getCurrentUserProfile())
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateCurrentUserProfile()")
    class UpdateProfileTests {

        @Test
        @DisplayName("Should update only provided fields")
        void shouldUpdatePartialFields() {
            UpdateProfileRequest request = new UpdateProfileRequest();
            request.setFirstName("Updated");
            // lastName and username left null — should remain unchanged

            when(securityUtils.getCurrentUserEmail()).thenReturn(USER_EMAIL);
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.toProfileResponse(any())).thenReturn(testProfileResponse);

            userService.updateCurrentUserProfile(request);

            verify(userRepository).save(argThat(u ->
                u.getFirstName().equals("Updated") &&
                u.getLastName().equals("User") // unchanged
            ));
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException for taken username")
        void shouldThrowForDuplicateUsername() {
            UpdateProfileRequest request = new UpdateProfileRequest();
            request.setUsername("takenuser");

            when(securityUtils.getCurrentUserEmail()).thenReturn(USER_EMAIL);
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(testUser));
            when(userRepository.existsByUsername("takenuser")).thenReturn(true);

            assertThatThrownBy(() -> userService.updateCurrentUserProfile(request))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("takenuser");

            verify(userRepository, never()).save(any());
        }
    }
}
