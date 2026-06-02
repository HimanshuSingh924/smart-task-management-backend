package com.taskmanager.tasks.dto;

import com.taskmanager.tasks.entity.Task;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for a single task — returned from all task endpoints.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Task response object")
public class TaskResponse {

    @Schema(description = "Task unique ID")
    private String id;

    @Schema(description = "Task title", example = "Implement login feature")
    private String title;

    @Schema(description = "Task description")
    private String description;

    @Schema(description = "Task status", example = "PENDING")
    private Task.TaskStatus status;

    @Schema(description = "Task priority", example = "HIGH")
    private Task.TaskPriority priority;

    @Schema(description = "Due date", example = "2025-12-31")
    private LocalDate dueDate;

    @Schema(description = "Owner user ID")
    private String ownerId;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last updated timestamp")
    private LocalDateTime updatedAt;
}
