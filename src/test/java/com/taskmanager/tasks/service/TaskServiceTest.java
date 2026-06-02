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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaskService — mocks all dependencies.
 * Verifies business logic, ownership enforcement, and error handling.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Unit Tests")
class TaskServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private UserRepository userRepository;
    @Mock private TaskMapper taskMapper;
    @Mock private SecurityUtils securityUtils;

    @InjectMocks
    private TaskService taskService;

    // ── Test Data ─────────────────────────────────────────────────────────

    private static final String USER_EMAIL = "test@example.com";
    private static final String USER_ID    = "user-001";
    private static final String TASK_ID    = "task-001";

    private User testUser;
    private Task testTask;
    private TaskResponse testTaskResponse;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .username("testuser")
                .role("ROLE_USER")
                .build();

        testTask = Task.builder()
                .id(TASK_ID)
                .title("Test Task")
                .description("Test Description")
                .status(Task.TaskStatus.PENDING)
                .priority(Task.TaskPriority.MEDIUM)
                .dueDate(LocalDate.now().plusDays(7))
                .ownerId(USER_ID)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testTaskResponse = TaskResponse.builder()
                .id(TASK_ID)
                .title("Test Task")
                .description("Test Description")
                .status(Task.TaskStatus.PENDING)
                .priority(Task.TaskPriority.MEDIUM)
                .ownerId(USER_ID)
                .build();
    }

    // ── createTask ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createTask()")
    class CreateTaskTests {

        @Test
        @DisplayName("Should create task and assign correct ownerId")
        void shouldCreateTaskSuccessfully() {
            // Arrange
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle("Test Task");
            request.setDescription("Test Description");
            request.setStatus(Task.TaskStatus.PENDING);
            request.setPriority(Task.TaskPriority.MEDIUM);
            request.setDueDate(LocalDate.now().plusDays(7));

            when(securityUtils.getCurrentUserEmail()).thenReturn(USER_EMAIL);
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(testUser));
            when(taskRepository.save(any(Task.class))).thenReturn(testTask);
            when(taskMapper.toResponse(testTask)).thenReturn(testTaskResponse);

            // Act
            TaskResponse result = taskService.createTask(request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(TASK_ID);
            assertThat(result.getTitle()).isEqualTo("Test Task");

            verify(taskRepository).save(argThat(task ->
                task.getOwnerId().equals(USER_ID) &&
                task.getTitle().equals("Test Task")
            ));
        }

        @Test
        @DisplayName("Should throw UnauthorizedException when user not in DB")
        void shouldThrowWhenUserNotFound() {
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle("Test");
            request.setStatus(Task.TaskStatus.PENDING);
            request.setPriority(Task.TaskPriority.LOW);

            when(securityUtils.getCurrentUserEmail()).thenReturn(USER_EMAIL);
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.createTask(request))
                    .isInstanceOf(UnauthorizedException.class);

            verify(taskRepository, never()).save(any());
        }
    }

    // ── getTaskById ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("getTaskById()")
    class GetTaskByIdTests {

        @Test
        @DisplayName("Should return task when owner requests it")
        void shouldReturnTaskForOwner() {
            when(securityUtils.getCurrentUserEmail()).thenReturn(USER_EMAIL);
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(testUser));
            when(taskRepository.findByIdAndOwnerId(TASK_ID, USER_ID)).thenReturn(Optional.of(testTask));
            when(taskMapper.toResponse(testTask)).thenReturn(testTaskResponse);

            TaskResponse result = taskService.getTaskById(TASK_ID);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(TASK_ID);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for non-existent or cross-user task")
        void shouldThrowForCrossUserAccess() {
            when(securityUtils.getCurrentUserEmail()).thenReturn(USER_EMAIL);
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(testUser));
            when(taskRepository.findByIdAndOwnerId(TASK_ID, USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.getTaskById(TASK_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(TASK_ID);
        }
    }

    // ── updateTask ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateTask()")
    class UpdateTaskTests {

        @Test
        @DisplayName("Should apply only non-null fields from update request")
        void shouldApplyPartialUpdate() {
            UpdateTaskRequest request = new UpdateTaskRequest();
            request.setTitle("Updated Title");
            request.setStatus(Task.TaskStatus.IN_PROGRESS);
            // description, priority, dueDate intentionally left null

            when(securityUtils.getCurrentUserEmail()).thenReturn(USER_EMAIL);
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(testUser));
            when(taskRepository.findByIdAndOwnerId(TASK_ID, USER_ID)).thenReturn(Optional.of(testTask));
            when(taskRepository.save(any(Task.class))).thenReturn(testTask);
            when(taskMapper.toResponse(any())).thenReturn(testTaskResponse);

            taskService.updateTask(TASK_ID, request);

            verify(taskRepository).save(argThat(task ->
                task.getTitle().equals("Updated Title") &&
                task.getStatus() == Task.TaskStatus.IN_PROGRESS &&
                task.getDescription().equals("Test Description") // unchanged
            ));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when task not found")
        void shouldThrowWhenTaskNotFound() {
            UpdateTaskRequest request = new UpdateTaskRequest();
            request.setTitle("New Title");

            when(securityUtils.getCurrentUserEmail()).thenReturn(USER_EMAIL);
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(testUser));
            when(taskRepository.findByIdAndOwnerId(TASK_ID, USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.updateTask(TASK_ID, request))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(taskRepository, never()).save(any());
        }
    }

    // ── deleteTask ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteTask()")
    class DeleteTaskTests {

        @Test
        @DisplayName("Should delete task when owner requests deletion")
        void shouldDeleteTaskSuccessfully() {
            when(securityUtils.getCurrentUserEmail()).thenReturn(USER_EMAIL);
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(testUser));
            when(taskRepository.findByIdAndOwnerId(TASK_ID, USER_ID)).thenReturn(Optional.of(testTask));

            taskService.deleteTask(TASK_ID);

            verify(taskRepository).delete(testTask);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when task not found")
        void shouldThrowWhenDeletingNonExistentTask() {
            when(securityUtils.getCurrentUserEmail()).thenReturn(USER_EMAIL);
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(testUser));
            when(taskRepository.findByIdAndOwnerId(TASK_ID, USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.deleteTask(TASK_ID))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(taskRepository, never()).delete(any());
        }
    }

    // ── getTasks ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getTasks()")
    class GetTasksTests {

        @Test
        @DisplayName("Should return paginated tasks with no filters")
        void shouldReturnPaginatedTasks() {
            Page<Task> taskPage = new PageImpl<>(List.of(testTask), PageRequest.of(0, 10), 1);

            when(securityUtils.getCurrentUserEmail()).thenReturn(USER_EMAIL);
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(testUser));
            when(taskRepository.findByOwnerId(eq(USER_ID), any(Pageable.class))).thenReturn(taskPage);
            when(taskMapper.toResponse(testTask)).thenReturn(testTaskResponse);

            Page<TaskResponse> result = taskService.getTasks(null, null, 0, 10, "createdAt", "desc");

            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getId()).isEqualTo(TASK_ID);
        }

        @Test
        @DisplayName("Should filter by status when status is provided")
        void shouldFilterByStatus() {
            Page<Task> taskPage = new PageImpl<>(List.of(testTask));

            when(securityUtils.getCurrentUserEmail()).thenReturn(USER_EMAIL);
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(testUser));
            when(taskRepository.findByOwnerIdAndStatus(eq(USER_ID), eq(Task.TaskStatus.PENDING), any()))
                    .thenReturn(taskPage);
            when(taskMapper.toResponse(testTask)).thenReturn(testTaskResponse);

            Page<TaskResponse> result = taskService.getTasks(Task.TaskStatus.PENDING, null, 0, 10, "createdAt", "desc");

            assertThat(result.getContent()).hasSize(1);
            verify(taskRepository).findByOwnerIdAndStatus(eq(USER_ID), eq(Task.TaskStatus.PENDING), any());
        }
    }
}
