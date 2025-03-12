package com.taskmanagement.service;

import com.taskmanagement.dto.TaskDTO;
import com.taskmanagement.exception.ResourceNotFound;
import com.taskmanagement.model.*;
import com.taskmanagement.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final EpicRepository epicRepository;
    private final SprintRepository sprintRepository;
    private final TaskStatusRepository statusRepository;
    private final TaskPriorityRepository priorityRepository;

    public TaskService(
            TaskRepository taskRepository,
            UserRepository userRepository,
            EpicRepository epicRepository,
            SprintRepository sprintRepository,
            TaskStatusRepository statusRepository,
            TaskPriorityRepository priorityRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.epicRepository = epicRepository;
        this.sprintRepository = sprintRepository;
        this.statusRepository = statusRepository;
        this.priorityRepository = priorityRepository;
    }

    @Transactional(readOnly = true)
    public List<TaskDTO> getAllTasks(UUID userId) {
        // In a real app, you might want to filter by user permissions
        return taskRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskDTO getTaskById(UUID id, UUID userId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("Task not found with id: " + id));

        return convertToDTO(task);
    }

    @Transactional
    public TaskDTO createTask(TaskDTO taskDTO, UUID creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFound("User not found with id: " + creatorId));

        User assignee = userRepository.findById(taskDTO.getAssignedToId())
                .orElseThrow(() -> new ResourceNotFound("Assigned user not found with id: " + taskDTO.getAssignedToId()));

        TaskStatus status = statusRepository.findById(taskDTO.getStatusId())
                .orElseThrow(() -> new ResourceNotFound("Status not found with id: " + taskDTO.getStatusId()));

        TaskPriority priority = priorityRepository.findById(taskDTO.getPriorityId())
                .orElseThrow(() -> new ResourceNotFound("Priority not found with id: " + taskDTO.getPriorityId()));

        Task task = new Task();
        task.setCreatedBy(creator);
        task.setAssignedTo(assignee);
        task.setStatus(status);
        task.setPriority(priority);
        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setStoryPoints(taskDTO.getStoryPoints());
        task.setEstimatedHours(taskDTO.getEstimatedHours());
        task.setDueDate(taskDTO.getDueDate());

        // Handle optional fields
        if (taskDTO.getEpicId() != null) {
            Epic epic = epicRepository.findById(taskDTO.getEpicId())
                    .orElseThrow(() -> new ResourceNotFound("Epic not found with id: " + taskDTO.getEpicId()));
            task.setEpic(epic);
        }

        if (taskDTO.getSprintId() != null) {
            Sprint sprint = sprintRepository.findById(taskDTO.getSprintId())
                    .orElseThrow(() -> new ResourceNotFound("Sprint not found with id: " + taskDTO.getSprintId()));
            task.setSprint(sprint);
        }

        // Check if task is marked as completed
        if (status.getName().equals("DONE")) {
            task.setCompletedAt(ZonedDateTime.now());
        }

        Task savedTask = taskRepository.save(task);
        return convertToDTO(savedTask);
    }

    @Transactional
    public TaskDTO updateTask(TaskDTO taskDTO, UUID updaterId) {
        Task existingTask = taskRepository.findById(taskDTO.getId())
                .orElseThrow(() -> new ResourceNotFound("Task not found with id: " + taskDTO.getId()));

        User assignee = userRepository.findById(taskDTO.getAssignedToId())
                .orElseThrow(() -> new ResourceNotFound("Assigned user not found with id: " + taskDTO.getAssignedToId()));

        TaskStatus status = statusRepository.findById(taskDTO.getStatusId())
                .orElseThrow(() -> new ResourceNotFound("Status not found with id: " + taskDTO.getStatusId()));

        TaskPriority priority = priorityRepository.findById(taskDTO.getPriorityId())
                .orElseThrow(() -> new ResourceNotFound("Priority not found with id: " + taskDTO.getPriorityId()));

        existingTask.setAssignedTo(assignee);
        existingTask.setStatus(status);
        existingTask.setPriority(priority);
        existingTask.setTitle(taskDTO.getTitle());
        existingTask.setDescription(taskDTO.getDescription());
        existingTask.setStoryPoints(taskDTO.getStoryPoints());
        existingTask.setEstimatedHours(taskDTO.getEstimatedHours());
        existingTask.setDueDate(taskDTO.getDueDate());

        // Handle optional fields
        if (taskDTO.getEpicId() != null) {
            Epic epic = epicRepository.findById(taskDTO.getEpicId())
                    .orElseThrow(() -> new ResourceNotFound("Epic not found with id: " + taskDTO.getEpicId()));
            existingTask.setEpic(epic);
        } else {
            existingTask.setEpic(null);
        }

        if (taskDTO.getSprintId() != null) {
            Sprint sprint = sprintRepository.findById(taskDTO.getSprintId())
                    .orElseThrow(() -> new ResourceNotFound("Sprint not found with id: " + taskDTO.getSprintId()));
            existingTask.setSprint(sprint);
        } else {
            existingTask.setSprint(null);
        }

        // Check if task is being marked as completed
        if (status.getName().equals("DONE") && existingTask.getCompletedAt() == null) {
            existingTask.setCompletedAt(ZonedDateTime.now());
        } else if (!status.getName().equals("DONE")) {
            existingTask.setCompletedAt(null);
        }

        Task updatedTask = taskRepository.save(existingTask);
        return convertToDTO(updatedTask);
    }

    @Transactional
    public void deleteTask(UUID id, UUID deleterId) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFound("Task not found with id: " + id);
        }

        taskRepository.deleteById(id);
    }

    private TaskDTO convertToDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStoryPoints(task.getStoryPoints());
        dto.setEstimatedHours(task.getEstimatedHours());
        dto.setDueDate(task.getDueDate());
        dto.setCompletedAt(task.getCompletedAt());

        dto.setCreatedById(task.getCreatedBy().getId());
        dto.setAssignedToId(task.getAssignedTo().getId());
        dto.setAssignedToName(task.getAssignedTo().getName());

        dto.setStatusId(task.getStatus().getId());
        dto.setStatusName(task.getStatus().getName());

        dto.setPriorityId(task.getPriority().getId());
        dto.setPriorityName(task.getPriority().getName());

        if (task.getEpic() != null) {
            dto.setEpicId(task.getEpic().getId());
            dto.setEpicName(task.getEpic().getName());
        }

        if (task.getSprint() != null) {
            dto.setSprintId(task.getSprint().getId());
            dto.setSprintName(task.getSprint().getName());
        }

        return dto;
    }
}