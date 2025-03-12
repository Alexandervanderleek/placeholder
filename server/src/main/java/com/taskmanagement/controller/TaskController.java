package com.taskmanagement.controller;

import com.taskmanagement.dto.TaskDTO;
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