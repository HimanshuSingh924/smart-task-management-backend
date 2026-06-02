package com.taskmanager.tasks.controller;

import com.taskmanager.common.response.ApiResponse;
import com.taskmanager.tasks.dto.CreateTaskRequest;
import com.taskmanager.tasks.dto.TaskResponse;
import com.taskmanager.tasks.dto.UpdateTaskRequest;
import com.taskmanager.tasks.entity.Task;
import com.taskmanager.tasks.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Task management endpoints — all require a valid JWT token.
 *
 * Every endpoint is automatically scoped to the authenticated user's tasks.
 * Cross-user access is blocked at the service layer.
 */
@Tag(name = "Tasks", description = "Full task CRUD with search, filter, and pagination")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // ── Create ────────────────────────────────────────────────────────────

    @Operation(summary = "Create a new task",
               description = "Creates a task owned by the currently authenticated user")
    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @Valid @RequestBody CreateTaskRequest request) {
        TaskResponse task = taskService.createTask(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Task created successfully", task));
    }

    // ── List with Filter + Pagination ─────────────────────────────────────

    @Operation(summary = "List tasks",
               description = "Returns paginated tasks for the current user with optional status/priority filters")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getTasks(
            @Parameter(description = "Filter by status") @RequestParam(required = false) Task.TaskStatus status,
            @Parameter(description = "Filter by priority") @RequestParam(required = false) Task.TaskPriority priority,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction: asc or desc") @RequestParam(defaultValue = "desc") String sortDir) {

        Page<TaskResponse> tasks = taskService.getTasks(status, priority, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Tasks retrieved successfully", tasks));
    }

    // ── Search ────────────────────────────────────────────────────────────

    @Operation(summary = "Search tasks by keyword",
               description = "Searches task titles and descriptions for the given keyword")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> searchTasks(
            @Parameter(description = "Search keyword") @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<TaskResponse> results = taskService.searchTasks(q, page, size);
        return ResponseEntity.ok(ApiResponse.success("Search completed", results));
    }

    // ── Get by ID ─────────────────────────────────────────────────────────

    @Operation(summary = "Get task by ID",
               description = "Returns a single task — only if the current user owns it")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(
            @PathVariable String id) {
        TaskResponse task = taskService.getTaskById(id);
        return ResponseEntity.ok(ApiResponse.success("Task retrieved successfully", task));
    }

    // ── Update ────────────────────────────────────────────────────────────

    @Operation(summary = "Update a task",
               description = "Partially updates a task — only provided fields are changed")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable String id,
            @Valid @RequestBody UpdateTaskRequest request) {
        TaskResponse updated = taskService.updateTask(id, request);
        return ResponseEntity.ok(ApiResponse.success("Task updated successfully", updated));
    }

    // ── Delete ────────────────────────────────────────────────────────────

    @Operation(summary = "Delete a task",
               description = "Permanently deletes a task — only the owner can delete it")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @PathVariable String id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully"));
    }
}
