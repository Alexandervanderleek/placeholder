package com.taskmanagement.repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.taskmanagement.model.Task;
import com.taskmanagement.model.User;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    // Find tasks by assigned user
    List<Task> findByAssignedTo(User assignedTo);
    
    // Find tasks by creator
    List<Task> findByCreatedBy(User createdBy);
    
    // Find tasks by status name
    @Query("SELECT t FROM Task t JOIN t.status s WHERE s.name = :statusName")
    List<Task> findByStatusName(@Param("statusName") String statusName);
    
    // Find tasks by priority name
    @Query("SELECT t FROM Task t JOIN t.priority p WHERE p.name = :priorityName")
    List<Task> findByPriorityName(@Param("priorityName") String priorityName);
    
    // Find tasks by epic id
    List<Task> findByEpicId(UUID epicId);
    
    // Find tasks by sprint id
    List<Task> findBySprintId(UUID sprintId);
    
    // Find overdue tasks
    List<Task> findByDueDateBeforeAndCompletedAtIsNull(ZonedDateTime now);
    
    // Find recently updated tasks
    List<Task> findByUpdatedAtAfterOrderByUpdatedAtDesc(ZonedDateTime since);
    
    // Find tasks for user dashboard (assigned to them and not completed)
    @Query("SELECT t FROM Task t WHERE t.assignedTo.id = :userId AND (t.status.name != 'DONE' OR t.completedAt IS NULL)")
    List<Task> findUserActiveTasks(@Param("userId") UUID userId);
    
    // Find tasks by multiple filters
    @Query("SELECT t FROM Task t " +
           "WHERE (:assignedToId IS NULL OR t.assignedTo.id = :assignedToId) " +
           "AND (:statusId IS NULL OR t.status.id = :statusId) " +
           "AND (:priorityId IS NULL OR t.priority.id = :priorityId) " +
           "AND (:sprintId IS NULL OR t.sprint.id = :sprintId) " +
           "AND (:epicId IS NULL OR t.epic.id = :epicId)")
    List<Task> findTasksByFilters(
            @Param("assignedToId") UUID assignedToId,
            @Param("statusId") UUID statusId,
            @Param("priorityId") UUID priorityId,
            @Param("sprintId") UUID sprintId,
            @Param("epicId") UUID epicId);
    
    // Count tasks by status for sprint
    @Query("SELECT COUNT(t) FROM Task t WHERE t.sprint.id = :sprintId AND t.status.id = :statusId")
    long countTasksBySprintAndStatus(@Param("sprintId") UUID sprintId, @Param("statusId") UUID statusId);
    
    // Check if task exists for a user
    boolean existsByIdAndAssignedToId(UUID taskId, UUID userId);
    
    // Task history for a specific task (for audit purposes)
    @Query(value = "SELECT * FROM task_history WHERE task_id = :taskId ORDER BY changed_at DESC", nativeQuery = true)
    List<Object[]> getTaskHistory(@Param("taskId") UUID taskId);
}