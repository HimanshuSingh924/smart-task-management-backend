package com.taskmanager.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response body for successful authentication (register or login).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication response containing the JWT token")
public class AuthResponse {

    @Schema(description = "JWT Bearer token", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "Token type", example = "Bearer")
    private String type;

    @Schema(description = "Authenticated user's email", example = "john@example.com")
    private String email;

    @Schema(description = "Authenticated user's username", example = "john_doe")
    private String username;

    @Schema(description = "User's role", example = "ROLE_USER")
    private String role;

    @Schema(description = "Token expiry duration in milliseconds", example = "86400000")
    private long expiresIn;
}
