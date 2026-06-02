package com.taskmanager.users.service;

import com.taskmanager.common.exception.DuplicateResourceException;
import com.taskmanager.common.exception.ResourceNotFoundException;
import com.taskmanager.common.mapper.UserMapper;
import com.taskmanager.common.util.SecurityUtils;
import com.taskmanager.users.dto.UpdateProfileRequest;
import com.taskmanager.users.dto.UserProfileResponse;
import com.taskmanager.users.entity.User;
import com.taskmanager.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Business logic for user profile operations.
 * Users can only read/update their own profile.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final SecurityUtils securityUtils;

    /**
     * Returns the profile of the currently authenticated user.
     */
    public UserProfileResponse getCurrentUserProfile() {
        String email = securityUtils.getCurrentUserEmail();
        User user = findUserByEmail(email);
        return userMapper.toProfileResponse(user);
    }

    /**
     * Updates the profile of the currently authenticated user.
     * Only firstName, lastName, and username can be updated.
     */
    public UserProfileResponse updateCurrentUserProfile(UpdateProfileRequest request) {
        String email = securityUtils.getCurrentUserEmail();
        User user = findUserByEmail(email);

        // Validate username uniqueness if being changed
        if (request.getUsername() != null &&
            !request.getUsername().equals(user.getUsername()) &&
            userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException(
                    "Username '" + request.getUsername() + "' is already taken");
        }

        // Apply updates (only non-null fields)
        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getUsername() != null) user.setUsername(request.getUsername());

        User savedUser = userRepository.save(user);
        log.info("Profile updated for user: {}", email);
        return userMapper.toProfileResponse(savedUser);
    }

    // ── Private Helpers ───────────────────────────────────────────────────

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", "email", email));
    }
}
