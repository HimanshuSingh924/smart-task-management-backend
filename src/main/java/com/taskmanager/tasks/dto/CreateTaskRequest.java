package com.taskmanager.tasks.dto;

import com.taskmanager.tasks.entity.Task;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * Request body for POST /api/v1/tasks
 */
@Data
@Schema(description = "Create task request payload")
public class CreateTaskRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    @Schema(description = "Task title", example = "Implement login feature")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Schema(description = "Detailed description", example = "Build the login page with JWT auth")
    private String description;

    @Schema(description = "Task status", example = "PENDING", allowableValues = {"PENDING", "IN_PROGRESS", "COMPLETED"})
    private Task.TaskStatus status;

    @NotNull(message = "Priority is required")
    @Schema(description = "Task priority", example = "HIGH", allowableValues = {"LOW", "MEDIUM", "HIGH"})
    private Task.TaskPriority priority;

    @FutureOrPresent(message = "Due date must be today or in the future")
    @Schema(description = "Due date (ISO format)", example = "2025-12-31")
    private LocalDate dueDate;
}
