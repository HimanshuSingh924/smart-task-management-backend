package com.taskmanager.common.mapper;

import com.taskmanager.users.dto.UserProfileResponse;
import com.taskmanager.users.entity.User;
import org.springframework.stereotype.Component;

/**
 * Maps between User entity and User DTOs.
 */
@Component
public class UserMapper {

    public UserProfileResponse toProfileResponse(User user) {
        if (user == null) return null;
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
