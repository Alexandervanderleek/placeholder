package com.taskmanagement.service;

import com.taskmanagement.dto.TaskDTO;
import com.taskmanagement.dto.TaskFilterDTO;
import com.taskmanagement.exception.ResourceNotFound;
import com.taskmanagement.exception.UnauthorizedAccessException;
import com.taskmanagement.model.*;
import com.taskmanagement.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TaskService {
    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

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
        logger.debug("Fetching all tasks for user: {}", userId);
        return taskRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksByFilter(TaskFilterDTO filterDTO, UUID userId) {
        logger.debug("Fetching tasks by filter for user: {}", userId);
        
        List<Task> filteredTasks = taskRepository.findTasksByFilters(
            filterDTO.getAssignedToId(),
            filterDTO.getStatusId(),
            filterDTO.getPriorityId(),
            filterDTO.getSprintId(),
            filterDTO.getEpicId()
        );
        
        return filteredTasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksByAssignee(UUID assigneeId, UUID requesterId) {
        logger.debug("Fetching tasks assigned to user: {}", assigneeId);
        
        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new ResourceNotFound("User not found with id: " + assigneeId));
        
        return taskRepository.findByAssignedTo(assignee).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskDTO> getUserActiveTasks(UUID userId) {
        logger.debug("Fetching active tasks for user: {}", userId);
        
        return taskRepository.findUserActiveTasks(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksByEpic(UUID epicId, UUID userId) {
        logger.debug("Fetching tasks for epic: {}", epicId);
        
        if (!epicRepository.existsById(epicId)) {
            throw new ResourceNotFound("Epic not found with id: " + epicId);
        }
        
        return taskRepository.findByEpicId(epicId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksBySprint(UUID sprintId, UUID userId) {
        logger.debug("Fetching tasks for sprint: {}", sprintId);
        
        if (!sprintRepository.existsById(sprintId)) {
            throw new ResourceNotFound("Sprint not found with id: " + sprintId);
        }
        
        return taskRepository.findBySprintId(sprintId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getSprintStats(UUID sprintId, UUID userId) {
        logger.debug("Calculating sprint statistics for sprint: {}", sprintId);
        
        if (!sprintRepository.existsById(sprintId)) {
            throw new ResourceNotFound("Sprint not found with id: " + sprintId);
        }
        
        // Get all statuses
        List<TaskStatus> statuses = statusRepository.findAll();
        
        // Count tasks for each status in this sprint
        Map<String, Long> statusCounts = statuses.stream()
                .collect(Collectors.toMap(
                    TaskStatus::getName,
                    status -> taskRepository.countTasksBySprintAndStatus(sprintId, status.getId())
                ));
        
        return statusCounts;
    }

    @Transactional(readOnly = true)
    public TaskDTO getTaskById(UUID id, UUID userId) {
        logger.debug("Fetching task: {} for user: {}", id, userId);
        
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("Task not found with id: " + id));

        return convertToDTO(task);
    }

    @Transactional
    public TaskDTO createTask(TaskDTO taskDTO, UUID creatorId) {
        logger.debug("Creating new task by user: {}", creatorId);
        
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
        logger.info("Created new task with ID: {}", savedTask.getId());
        
        return convertToDTO(savedTask);
    }

    @Transactional
    public TaskDTO updateTask(TaskDTO taskDTO, UUID updaterId) {
        logger.debug("Updating task: {} by user: {}", taskDTO.getId(), updaterId);
        
        Task existingTask = taskRepository.findById(taskDTO.getId())
                .orElseThrow(() -> new ResourceNotFound("Task not found with id: " + taskDTO.getId()));

        // Authorization is now done at controller level with @PreAuthorize
        
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
            logger.info("Task {} marked as completed", existingTask.getId());
        } else if (!status.getName().equals("DONE")) {
            existingTask.setCompletedAt(null);
        }

        Task updatedTask = taskRepository.save(existingTask);
        logger.info("Updated task with ID: {}", updatedTask.getId());
        
        return convertToDTO(updatedTask);
    }

    @Transactional
    public TaskDTO changeTaskStatus(UUID taskId, UUID statusId, UUID userId) {
        logger.debug("Changing status of task: {} to status: {} by user: {}", taskId, statusId, userId);
        
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFound("Task not found with id: " + taskId));
        
        // Authorization is now done at controller level with @PreAuthorize
                
        TaskStatus newStatus = statusRepository.findById(statusId)
                .orElseThrow(() -> new ResourceNotFound("Status not found with id: " + statusId));
        
        // Save old status for logging
        String oldStatusName = task.getStatus().getName();
        
        // Update status
        task.setStatus(newStatus);
        
        // Update completion time if status is DONE
        if (newStatus.getName().equals("DONE") && task.getCompletedAt() == null) {
            task.setCompletedAt(ZonedDateTime.now());
            logger.info("Task {} marked as completed", task.getId());
        } else if (!newStatus.getName().equals("DONE")) {
            task.setCompletedAt(null);
        }
        
        Task updatedTask = taskRepository.save(task);
        logger.info("Changed task {} status from {} to {}", 
                updatedTask.getId(), oldStatusName, newStatus.getName());
        
        return convertToDTO(updatedTask);
    }

    @Transactional
    public TaskDTO assignTask(UUID taskId, UUID assigneeId, UUID userId) {
        logger.debug("Assigning task: {} to user: {} by user: {}", taskId, assigneeId, userId);
        
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFound("Task not found with id: " + taskId));
        
        // Authorization is now done at controller level with @PreAuthorize
                
        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new ResourceNotFound("User not found with id: " + assigneeId));
        
        // Save old assignee for logging
        UUID oldAssigneeId = task.getAssignedTo().getId();
        
        // Update assignee
        task.setAssignedTo(assignee);
        
        Task updatedTask = taskRepository.save(task);
        logger.info("Assigned task {} from user {} to user {}", 
                updatedTask.getId(), oldAssigneeId, assignee.getId());
        
        return convertToDTO(updatedTask);
    }

    @Transactional
    public TaskDTO addTaskToSprint(UUID taskId, UUID sprintId, UUID userId) {
        logger.debug("Adding task: {} to sprint: {} by user: {}", taskId, sprintId, userId);
        
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFound("Task not found with id: " + taskId));
        
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFound("Sprint not found with id: " + sprintId));
        
        // Authorization is now done at controller level with @PreAuthorize
        
        // Check if sprint is active
        if (!sprint.isActive()) {
            logger.warn("User {} attempted to add task {} to inactive sprint {}", userId, taskId, sprintId);
            throw new IllegalStateException("Cannot add tasks to inactive sprints");
        }
        
        // Save the previous sprint ID for logging
        UUID previousSprintId = task.getSprint() != null ? task.getSprint().getId() : null;
        
        // Update task with new sprint
        task.setSprint(sprint);
        
        Task updatedTask = taskRepository.save(task);
        logger.info("Added task {} to sprint {} (previous sprint: {})", 
                taskId, sprintId, previousSprintId);
        
        return convertToDTO(updatedTask);
    }
    
    @Transactional
    public TaskDTO removeTaskFromSprint(UUID taskId, UUID userId) {
        logger.debug("Removing task: {} from sprint by user: {}", taskId, userId);
        
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFound("Task not found with id: " + taskId));
        
        // Authorization is now done at controller level with @PreAuthorize
        
        // Check if task is actually in a sprint
        if (task.getSprint() == null) {
            logger.warn("User {} attempted to remove task {} that is not in any sprint", userId, taskId);
            throw new IllegalStateException("Task is not assigned to any sprint");
        }
        
        // Save the previous sprint ID for logging
        UUID previousSprintId = task.getSprint().getId();
        
        // Remove task from sprint
        task.setSprint(null);
        
        Task updatedTask = taskRepository.save(task);
        logger.info("Removed task {} from sprint {}", taskId, previousSprintId);
        
        return convertToDTO(updatedTask);
    }
    
    @Transactional
    public TaskDTO addTaskToEpic(UUID taskId, UUID epicId, UUID userId) {
        logger.debug("Adding task: {} to epic: {} by user: {}", taskId, epicId, userId);
        
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFound("Task not found with id: " + taskId));
        
        Epic epic = epicRepository.findById(epicId)
                .orElseThrow(() -> new ResourceNotFound("Epic not found with id: " + epicId));
        
        // Authorization is now done at controller level with @PreAuthorize
        
        // Save the previous epic ID for logging
        UUID previousEpicId = task.getEpic() != null ? task.getEpic().getId() : null;
        
        // Update task with new epic
        task.setEpic(epic);
        
        Task updatedTask = taskRepository.save(task);
        logger.info("Added task {} to epic {} (previous epic: {})", 
                taskId, epicId, previousEpicId);
        
        return convertToDTO(updatedTask);
    }
    
    @Transactional
    public TaskDTO removeTaskFromEpic(UUID taskId, UUID userId) {
        logger.debug("Removing task: {} from epic by user: {}", taskId, userId);
        
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFound("Task not found with id: " + taskId));
        
        // Authorization is now done at controller level with @PreAuthorize
        
        // Check if task is actually in an epic
        if (task.getEpic() == null) {
            logger.warn("User {} attempted to remove task {} that is not in any epic", userId, taskId);
            throw new IllegalStateException("Task is not assigned to any epic");
        }
        
        // Save the previous epic ID for logging
        UUID previousEpicId = task.getEpic().getId();
        
        // Remove task from epic
        task.setEpic(null);
        
        Task updatedTask = taskRepository.save(task);
        logger.info("Removed task {} from epic {}", taskId, previousEpicId);
        
        return convertToDTO(updatedTask);
    }

    @Transactional
    public void deleteTask(UUID id, UUID deleterId) {
        logger.debug("Deleting task: {} by user: {}", id, deleterId);
        
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFound("Task not found with id: " + id);
        }
        
        // Authorization is handled by @PreAuthorize on the controller and TaskSecurity service

        taskRepository.deleteById(id);
        logger.info("Deleted task with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public List<TaskDTO> getOverdueTasks(UUID userId) {
        logger.debug("Fetching overdue tasks for user: {}", userId);
        
        ZonedDateTime now = ZonedDateTime.now();
        
        return taskRepository.findByDueDateBeforeAndCompletedAtIsNull(now).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskDTO> getRecentlyUpdatedTasks(UUID userId, int hoursAgo) {
        logger.debug("Fetching tasks updated in the last {} hours for user: {}", hoursAgo, userId);
        
        ZonedDateTime since = ZonedDateTime.now().minusHours(hoursAgo);
        
        return taskRepository.findByUpdatedAtAfterOrderByUpdatedAtDesc(since).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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