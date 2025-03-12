package com.taskmanagement.service;

import com.taskmanagement.dto.TaskDTO;
import com.taskmanagement.exception.ResourceNotFound;
import com.taskmanagement.model.*;
import com.taskmanagement.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EpicRepository epicRepository;

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private TaskStatusRepository statusRepository;

    @Mock
    private TaskPriorityRepository priorityRepository;

    @InjectMocks
    private TaskService taskService;

    private UUID userId;
    private UUID taskId;
    private User user;
    private TaskStatus status;
    private TaskPriority priority;
    private Task task;
    private TaskDTO taskDTO;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        taskId = UUID.randomUUID();

        // Setup user
        user = new User();
        user.setId(userId);
        user.setName("Test User");
        user.setEmail("test@example.com");

        // Setup task status
        status = new TaskStatus();
        status.setId(UUID.randomUUID());
        status.setName("TODO");
        status.setDisplayOrder(1);

        // Setup task priority
        priority = new TaskPriority();
        priority.setId(UUID.randomUUID());
        priority.setName("MEDIUM");
        priority.setValue(2);

        // Setup task
        task = new Task();
        task.setId(taskId);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setCreatedBy(user);
        task.setAssignedTo(user);
        task.setStatus(status);
        task.setPriority(priority);
        task.setStoryPoints(3);
        task.setEstimatedHours(8);
        task.setDueDate(ZonedDateTime.now().plusDays(7));
        task.setCreatedAt(ZonedDateTime.now());
        task.setUpdatedAt(ZonedDateTime.now());

        // Setup task DTO
        taskDTO = new TaskDTO();
        taskDTO.setTitle("Test Task");
        taskDTO.setDescription("Test Description");
        taskDTO.setAssignedToId(userId);
        taskDTO.setStatusId(status.getId());
        taskDTO.setPriorityId(priority.getId());
        taskDTO.setStoryPoints(3);
        taskDTO.setEstimatedHours(8);
        taskDTO.setDueDate(ZonedDateTime.now().plusDays(7));
    }

    @Test
    void getAllTasks_ShouldReturnAllTasks() {
        // Arrange
        when(taskRepository.findAll()).thenReturn(Arrays.asList(task));

        // Act
        List<TaskDTO> result = taskService.getAllTasks(userId);

        // Assert
        assertEquals(1, result.size());
        assertEquals(taskId, result.get(0).getId());
        assertEquals("Test Task", result.get(0).getTitle());
        verify(taskRepository).findAll();
    }

    @Test
    void getTaskById_WithValidId_ShouldReturnTask() {
        // Arrange
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        // Act
        TaskDTO result = taskService.getTaskById(taskId, userId);

        // Assert
        assertNotNull(result);
        assertEquals(taskId, result.getId());
        assertEquals("Test Task", result.getTitle());
        verify(taskRepository).findById(taskId);
    }

    @Test
    void getTaskById_WithInvalidId_ShouldThrowException() {
        // Arrange
        UUID invalidId = UUID.randomUUID();
        when(taskRepository.findById(invalidId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFound.class, () -> taskService.getTaskById(invalidId, userId));
        verify(taskRepository).findById(invalidId);
    }

    @Test
    void createTask_ShouldCreateAndReturnTask() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(statusRepository.findById(status.getId())).thenReturn(Optional.of(status));
        when(priorityRepository.findById(priority.getId())).thenReturn(Optional.of(priority));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // Act
        TaskDTO result = taskService.createTask(taskDTO, userId);

        // Assert
        assertNotNull(result);
        assertEquals("Test Task", result.getTitle());
        assertEquals("Test Description", result.getDescription());
        verify(userRepository, times(2)).findById(userId); // Creator and assignee
        verify(statusRepository).findById(status.getId());
        verify(priorityRepository).findById(priority.getId());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void updateTask_WithValidId_ShouldUpdateAndReturnTask() {
        // Arrange
        UUID updatedTaskId = taskId;
        taskDTO.setId(updatedTaskId);
        taskDTO.setTitle("Updated Task");
        
        when(taskRepository.findById(updatedTaskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(statusRepository.findById(status.getId())).thenReturn(Optional.of(status));
        when(priorityRepository.findById(priority.getId())).thenReturn(Optional.of(priority));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task savedTask = invocation.getArgument(0);
            savedTask.setTitle("Updated Task");
            return savedTask;
        });

        // Act
        TaskDTO result = taskService.updateTask(taskDTO, userId);

        // Assert
        assertNotNull(result);
        assertEquals(updatedTaskId, result.getId());
        assertEquals("Updated Task", result.getTitle());
        verify(taskRepository).findById(updatedTaskId);
        verify(userRepository).findById(userId);
        verify(statusRepository).findById(status.getId());
        verify(priorityRepository).findById(priority.getId());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void deleteTask_WithValidId_ShouldDeleteTask() {
        // Arrange
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        doNothing().when(taskRepository).deleteById(taskId);

        // Act
        taskService.deleteTask(taskId, userId);

        // Assert
        verify(taskRepository).findById(taskId);
        verify(taskRepository).deleteById(taskId);
    }

    @Test
    void getUserActiveTasks_ShouldReturnActiveTasks() {
        // Arrange
        when(taskRepository.findUserActiveTasks(userId)).thenReturn(Arrays.asList(task));

        // Act
        List<TaskDTO> result = taskService.getUserActiveTasks(userId);

        // Assert
        assertEquals(1, result.size());
        assertEquals(taskId, result.get(0).getId());
        verify(taskRepository).findUserActiveTasks(userId);
    }

    @Test
    void changeTaskStatus_ShouldUpdateTaskStatus() {
        // Arrange
        UUID newStatusId = UUID.randomUUID();
        TaskStatus newStatus = new TaskStatus();
        newStatus.setId(newStatusId);
        newStatus.setName("IN_PROGRESS");
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(statusRepository.findById(newStatusId)).thenReturn(Optional.of(newStatus));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task savedTask = invocation.getArgument(0);
            savedTask.setStatus(newStatus);
            return savedTask;
        });

        // Act
        TaskDTO result = taskService.changeTaskStatus(taskId, newStatusId, userId);

        // Assert
        assertNotNull(result);
        assertEquals("IN_PROGRESS", result.getStatusName());
        verify(taskRepository).findById(taskId);
        verify(statusRepository).findById(newStatusId);
        verify(taskRepository).save(any(Task.class));
    }
}