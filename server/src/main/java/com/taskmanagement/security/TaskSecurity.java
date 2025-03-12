package com.taskmanagement.security;

import com.taskmanagement.model.Task;
import com.taskmanagement.repository.TaskRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service("taskSecurityService")
public class TaskSecurity {
    private final TaskRepository taskRepository;


    public TaskSecurity(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public boolean canDeleteTask(UUID taskId, Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());

        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            return false;
        }

        Task task = taskOpt.get();

        boolean isCreator = task.getCreatedBy().getId().equals(userId);

        boolean isBacklog = "BACKLOG".equals(task.getStatus().getName());

        return isCreator && isBacklog;

    }


}
