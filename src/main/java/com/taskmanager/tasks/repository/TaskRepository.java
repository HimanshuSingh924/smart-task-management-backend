package com.taskmanager.tasks.repository;

import com.taskmanager.tasks.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * MongoDB repository for Task documents with owner-scoped queries.
 * All query methods filter by ownerId to enforce data isolation.
 */
@Repository
public interface TaskRepository extends MongoRepository<Task, String> {

    /** Paginated list of all tasks for a given owner */
    Page<Task> findByOwnerId(String ownerId, Pageable pageable);

    /** Find task by id AND ownerId — prevents cross-user access */
    Optional<Task> findByIdAndOwnerId(String id, String ownerId);

    /** Filter by status */
    Page<Task> findByOwnerIdAndStatus(String ownerId, Task.TaskStatus status, Pageable pageable);

    /** Filter by priority */
    Page<Task> findByOwnerIdAndPriority(String ownerId, Task.TaskPriority priority, Pageable pageable);

    /** Filter by status AND priority */
    Page<Task> findByOwnerIdAndStatusAndPriority(
            String ownerId,
            Task.TaskStatus status,
            Task.TaskPriority priority,
            Pageable pageable);

    /** Full-text search on title and description */
    @Query("{ 'ownerId': ?0, $or: [ { 'title': { $regex: ?1, $options: 'i' } }, { 'description': { $regex: ?1, $options: 'i' } } ] }")
    Page<Task> searchByOwnerIdAndKeyword(String ownerId, String keyword, Pageable pageable);

    /** Count tasks by owner */
    long countByOwnerId(String ownerId);

    /** Count tasks by owner and status */
    long countByOwnerIdAndStatus(String ownerId, Task.TaskStatus status);

    /** Check existence by id and owner */
    boolean existsByIdAndOwnerId(String id, String ownerId);
}
