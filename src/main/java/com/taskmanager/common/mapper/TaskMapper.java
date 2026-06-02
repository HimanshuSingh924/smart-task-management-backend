package com.taskmanager.common.mapper;

import com.taskmanager.tasks.dto.TaskResponse;
import com.taskmanager.tasks.entity.Task;
import org.springframework.stereotype.Component;

/**
 * Maps between Task entity and Task DTOs.
 * Kept as a plain Spring @Component to avoid MapStruct dependency complexity.
 */
@Component
public class TaskMapper {

    public TaskResponse toResponse(Task task) {
        if (task == null) return null;
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .ownerId(task.getOwnerId())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
