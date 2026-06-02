package com.taskmanager.tasks.dto;

import com.taskmanager.tasks.entity.Task;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * Request body for PUT /api/v1/tasks/{id}
 * All fields are optional — only provided fields are updated.
 */
@Data
@Schema(description = "Update task request — all fields optional")
public class UpdateTaskRequest {

    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    @Schema(description = "New task title", example = "Implement login feature v2")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Schema(description = "Updated description")
    private String description;

    @Schema(description = "New status", allowableValues = {"PENDING", "IN_PROGRESS", "COMPLETED"})
    private Task.TaskStatus status;

    @Schema(description = "New priority", allowableValues = {"LOW", "MEDIUM", "HIGH"})
    private Task.TaskPriority priority;

    @Schema(description = "New due date", example = "2026-01-15")
    private LocalDate dueDate;
}
