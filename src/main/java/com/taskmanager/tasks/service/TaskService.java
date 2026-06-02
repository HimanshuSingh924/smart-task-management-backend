package com.taskmanager.tasks.service;

import com.taskmanager.common.exception.ResourceNotFoundException;
import com.taskmanager.common.exception.UnauthorizedException;
import com.taskmanager.common.mapper.TaskMapper;
import com.taskmanager.common.util.SecurityUtils;
import com.taskmanager.tasks.dto.CreateTaskRequest;
import com.taskmanager.tasks.dto.TaskResponse;
import com.taskmanager.tasks.dto.UpdateTaskRequest;
import com.taskmanager.tasks.entity.Task;
import com.taskmanager.tasks.repository.TaskRepository;
import com.taskmanager.users.entity.User;
import com.taskmanager.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Core task management business logic.
 *
 * OWNERSHIP RULE: Every operation is scoped to the authenticated user's ownerId.
 * A user can NEVER see, edit, or delete another user's tasks.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;
    private final SecurityUtils securityUtils;

    // ── Create ────────────────────────────────────────────────────────────

    /**
     * Creates a new task owned by the currently authenticated user.
     */
    public TaskResponse createTask(CreateTaskRequest request) {
        String ownerId = resolveCurrentUserId();

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : Task.TaskStatus.PENDING)
                .priority(request.getPriority())
                .dueDate(request.getDueDate())
                .ownerId(ownerId)
                .build();

        Task saved = taskRepository.save(task);
        log.info("Task created: {} by owner: {}", saved.getId(), ownerId);
        return taskMapper.toResponse(saved);
    }

    // ── Read ──────────────────────────────────────────────────────────────

    /**
     * Returns a paginated, optionally filtered list of tasks for the current user.
     */
    public Page<TaskResponse> getTasks(
            Task.TaskStatus status,
            Task.TaskPriority priority,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        String ownerId = resolveCurrentUserId();
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Task> tasks;

        if (status != null && priority != null) {
            tasks = taskRepository.findByOwnerIdAndStatusAndPriority(ownerId, status, priority, pageable);
        } else if (status != null) {
            tasks = taskRepository.findByOwnerIdAndStatus(ownerId, status, pageable);
        } else if (priority != null) {
            tasks = taskRepository.findByOwnerIdAndPriority(ownerId, priority, pageable);
        } else {
            tasks = taskRepository.findByOwnerId(ownerId, pageable);
        }

        return tasks.map(taskMapper::toResponse);
    }

    /**
     * Returns a single task by ID — only if the current user owns it.
     */
    public TaskResponse getTaskById(String taskId) {
        String ownerId = resolveCurrentUserId();
        Task task = findTaskByIdAndOwner(taskId, ownerId);
        return taskMapper.toResponse(task);
    }

    /**
     * Searches tasks by keyword in title or description.
     */
    public Page<TaskResponse> searchTasks(String keyword, int page, int size) {
        String ownerId = resolveCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        if (!StringUtils.hasText(keyword)) {
            return taskRepository.findByOwnerId(ownerId, pageable)
                    .map(taskMapper::toResponse);
        }

        return taskRepository
                .searchByOwnerIdAndKeyword(ownerId, keyword, pageable)
                .map(taskMapper::toResponse);
    }

    // ── Update ────────────────────────────────────────────────────────────

    /**
     * Updates a task — only the owner can update their task.
     * Only non-null fields in the request are applied.
     */
    public TaskResponse updateTask(String taskId, UpdateTaskRequest request) {
        String ownerId = resolveCurrentUserId();
        Task task = findTaskByIdAndOwner(taskId, ownerId);

        if (request.getTitle() != null) task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getStatus() != null) task.setStatus(request.getStatus());
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        if (request.getDueDate() != null) task.setDueDate(request.getDueDate());

        Task saved = taskRepository.save(task);
        log.info("Task updated: {} by owner: {}", taskId, ownerId);
        return taskMapper.toResponse(saved);
    }

    // ── Delete ────────────────────────────────────────────────────────────

    /**
     * Deletes a task — only the owner can delete their task.
     */
    public void deleteTask(String taskId) {
        String ownerId = resolveCurrentUserId();
        Task task = findTaskByIdAndOwner(taskId, ownerId);
        taskRepository.delete(task);
        log.info("Task deleted: {} by owner: {}", taskId, ownerId);
    }

    // ── Private Helpers ───────────────────────────────────────────────────

    /**
     * Resolves the current user's MongoDB ID from their email in the SecurityContext.
     */
    private String resolveCurrentUserId() {
        String email = securityUtils.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException(
                        "Authenticated user not found in database: " + email));
        return user.getId();
    }

    /**
     * Finds a task by ID and validates ownership.
     * Returns ResourceNotFoundException if not found (regardless of owner).
     */
    private Task findTaskByIdAndOwner(String taskId, String ownerId) {
        return taskRepository.findByIdAndOwnerId(taskId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Task not found with id: '" + taskId + "'"));
    }
}
