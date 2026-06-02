package com.taskmanager.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for PUT /api/v1/users/profile
 * Only updatable fields are exposed — email and role cannot be changed here.
 */
@Data
@Schema(description = "Update user profile request")
public class UpdateProfileRequest {

    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    @Schema(description = "Updated first name", example = "Jonathan")
    private String firstName;

    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    @Schema(description = "Updated last name", example = "Doering")
    private String lastName;

    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
    @Schema(description = "Updated username", example = "jon_doe")
    private String username;
}
