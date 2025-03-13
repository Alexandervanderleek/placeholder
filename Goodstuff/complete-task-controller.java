package com.taskmanagement.controller;

import com.taskmanagement.dto.TaskDTO;
import com.taskmanagement.dto.TaskFilterDTO;
import com.taskmanagement.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<List<TaskDTO>> getAllTasks(@AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        logger.info("User {} requesting all tasks", userId);
        return ResponseEntity.ok(taskService.getAllTasks(userId));
    }

    @PostMapping("/filter")
    public ResponseEntity<List<TaskDTO>> getTasksByFilter(
            @RequestBody TaskFilterDTO filterDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        logger.info("User {} filtering tasks", userId);
        return ResponseEntity.ok(taskService.getTasksByFilter(filterDTO, userId));
    }

    @GetMapping("/assignee/{assigneeId}")
    public ResponseEntity<List<TaskDTO>> getTasksByAssignee(
            @PathVariable UUID assigneeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        logger.info("User {} requesting tasks for assignee {}", userId, assigneeId);
        return ResponseEntity.ok(taskService.getTasksByAssignee(assigneeId, userId));
    }

    @GetMapping("/my-tasks")
    public ResponseEntity<List<TaskDTO>> getMyActiveTasks(@AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        logger.info("User {} requesting their active tasks", userId);
        return ResponseEntity.ok(taskService.getUserActiveTasks(userId));
    }

    @GetMapping("/epic/{epicId}")
    public ResponseEntity<List<TaskDTO>> getTasksByEpic(
            @PathVariable UUID epicId,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        logger.info("User {} requesting tasks for epic {}", userId, epicId);
        return ResponseEntity.ok(taskService.getTasksByEpic(epicId, userId));
    }

    @GetMapping("/sprint/{sprintId}")
    public ResponseEntity<List<TaskDTO>> getTasksBySprint(
            @PathVariable UUID sprintId,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        logger.info("User {} requesting tasks for sprint {}", userId, sprintId);
        return ResponseEntity.ok(taskService.getTasksBySprint(sprintId, userId));
    }

    @GetMapping("/sprint/{sprintId}/stats")
    public ResponseEntity<Map<String, Long>> getSprintStats(
            @PathVariable UUID sprintId,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        logger.info("User {} requesting stats for sprint {}", userId, sprintId);
        return ResponseEntity.ok(taskService.getSprintStats(sprintId, userId));
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<TaskDTO>> getOverdueTasks(@AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        logger.info("User {} requesting overdue tasks", userId);
        return ResponseEntity.ok(taskService.getOverdueTasks(userId));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<TaskDTO>> getRecentlyUpdatedTasks(
            @RequestParam(defaultValue = "24") int hours,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        logger.info("User {} requesting tasks updated in the last {} hours", userId, hours);
        return ResponseEntity.ok(taskService.getRecentlyUpdatedTasks(userId, hours));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getTaskById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        logger.info("User {} requesting task {}", userId, id);
        return ResponseEntity.ok(taskService.getTaskById(id, userId));
    }

    @PostMapping
    public ResponseEntity<TaskDTO> createTask(
            @Valid @RequestBody TaskDTO taskDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        logger.info("User {} creating new task", userId);
        return ResponseEntity.ok(taskService.createTask(taskDTO, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> updateTask(
            @PathVariable UUID id,
            @Valid @RequestBody TaskDTO taskDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        logger.info("User {} updating task {}", userId, id);
        taskDTO.setId(id);
        return ResponseEntity.ok(taskService.updateTask(taskDTO, userId));
    }

    @PatchMapping("/{id}/status/{statusId}")
    public ResponseEntity<TaskDTO> changeTaskStatus(
            @PathVariable UUID id,
            @PathVariable UUID statusId,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        logger.info("User {} changing status of task {} to status {}", userId, id, statusId);
        return ResponseEntity.ok(taskService.changeTaskStatus(id, statusId, userId));
    }

    @PatchMapping("/{id}/assign/{assigneeId}")
    public ResponseEntity<TaskDTO> assignTask(
            @PathVariable UUID id,
            @PathVariable UUID assigneeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        logger.info("User {} assigning task {} to user {}", userId, id, assigneeId);
        return ResponseEntity.ok(taskService.assignTask(id, assigneeId, userId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SCRUM_MASTER') or hasRole('ADMIN') or @taskSecurityService.canDeleteTask(#id, authentication)")
    public ResponseEntity<Void> deleteTask(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        logger.info("User {} deleting task {}", userId, id);
        taskService.deleteTask(id, userId);
        return ResponseEntity.noContent().build();
    }
}