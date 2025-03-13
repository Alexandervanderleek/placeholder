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
    private UUID epicId;
    private UUID sprintId;
    private UUID statusId;
    private User user;
    private TaskStatus todoStatus;
    private TaskStatus doneStatus;
    private TaskPriority priority;
    private Epic epic;
    private Sprint sprint;
    private Task task;
    private TaskDTO taskDTO;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        taskId = UUID.randomUUID();
        epicId = UUID.randomUUID();
        sprintId = UUID.randomUUID();
        statusId = UUID.randomUUID();

        // Setup user
        user = new User();
        user.setId(userId);
        user.setName("Test User");
        user.setEmail("test@example.com");

        // Setup task status
        todoStatus = new TaskStatus();
        todoStatus.setId(UUID.randomUUID());
        todoStatus.setName("TODO");
        todoStatus.setDisplayOrder(1);
        
        doneStatus = new TaskStatus();
        doneStatus.setId(UUID.randomUUID());
        doneStatus.setName("DONE");
        doneStatus.setDisplayOrder(4);

        // Setup task priority
        priority = new TaskPriority();
        priority.setId(UUID.randomUUID());
        priority.setName("MEDIUM");
        priority.setValue(2);
        
        // Setup epic
        epic = new Epic();
        epic.setId(epicId);
        epic.setName("Test Epic");
        epic.setDescription("Test Epic Description");
        epic.setOwner(user);
        
        // Setup sprint
        sprint = new Sprint();
        sprint.setId(sprintId);
        sprint.setName("Test Sprint");
        sprint.setScrumMaster(user);
        sprint.setActive(true);

        // Setup task
        task = new Task();
        task.setId(taskId);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setCreatedBy(user);
        task.setAssignedTo(user);
        task.setStatus(todoStatus);
        task.setPriority(priority);
        task.setStoryPoints(3);
        task.setEstimatedHours(8);
        task.setDueDate(ZonedDateTime.now().plusDays(7));
        task.setCreatedAt(ZonedDateTime.now());
        task.setUpdatedAt(ZonedDateTime.now());

        // Setup task DTO
        taskDTO = new TaskDTO();
        taskDTO.setId(taskId);
        taskDTO.setTitle("Test Task");
        taskDTO.setDescription("Test Description");
        taskDTO.setAssignedToId(userId);
        taskDTO.setStatusId(todoStatus.getId());
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
        when(statusRepository.findById(any(UUID.class))).thenReturn(Optional.of(todoStatus));
        when(priorityRepository.findById(any(UUID.class))).thenReturn(Optional.of(priority));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // Act
        TaskDTO result = taskService.createTask(taskDTO, userId);

        // Assert
        assertNotNull(result);
        assertEquals("Test Task", result.getTitle());
        assertEquals("Test Description", result.getDescription());
        verify(userRepository, times(2)).findById(userId); // Creator and assignee
        verify(statusRepository).findById(any(UUID.class));
        verify(priorityRepository).findById(any(UUID.class));
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createTask_WithEpicAndSprint_ShouldCreateTaskWithRelationships() {
        // Arrange
        taskDTO.setEpicId(epicId);
        taskDTO.setSprintId(sprintId);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(statusRepository.findById(any(UUID.class))).thenReturn(Optional.of(todoStatus));
        when(priorityRepository.findById(any(UUID.class))).thenReturn(Optional.of(priority));
        when(epicRepository.findById(epicId)).thenReturn(Optional.of(epic));
        when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(sprint));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // Act
        TaskDTO result = taskService.createTask(taskDTO, userId);

        // Assert
        assertNotNull(result);
        verify(epicRepository).findById(epicId);
        verify(sprintRepository).findById(sprintId);
    }

    @Test
    void updateTask_WithValidId_ShouldUpdateAndReturnTask() {
        // Arrange
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(statusRepository.findById(any(UUID.class))).thenReturn(Optional.of(todoStatus));
        when(priorityRepository.findById(any(UUID.class))).thenReturn(Optional.of(priority));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // Act
        TaskDTO result = taskService.updateTask(taskDTO, userId);

        // Assert
        assertNotNull(result);
        assertEquals(taskId, result.getId());
        verify(taskRepository).findById(taskId);
        verify(userRepository).findById(userId);
        verify(statusRepository).findById(any(UUID.class));
        verify(priorityRepository).findById(any(UUID.class));
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void changeTaskStatus_ShouldUpdateTaskStatus() {
        // Arrange
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(statusRepository.findById(statusId)).thenReturn(Optional.of(doneStatus));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task savedTask = invocation.getArgument(0);
            savedTask.setStatus(doneStatus);
            return savedTask;
        });

        // Act
        TaskDTO result = taskService.changeTaskStatus(taskId, statusId, userId);

        // Assert
        assertNotNull(result);
        assertEquals("DONE", result.getStatusName());
        verify(taskRepository).findById(taskId);
        verify(statusRepository).findById(statusId);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void addTaskToSprint_ShouldAddTaskToSprint() {
        // Arrange
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(sprint));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task savedTask = invocation.getArgument(0);
            savedTask.setSprint(sprint);
            return savedTask;
        });

        // Act
        TaskDTO result = taskService.addTaskToSprint(taskId, sprintId, userId);

        // Assert
        assertNotNull(result);
        verify(taskRepository).findById(taskId);
        verify(sprintRepository).findById(sprintId);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void removeTaskFromSprint_ShouldRemoveTaskFromSprint() {
        // Arrange
        task.setSprint(sprint);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task savedTask = invocation.getArgument(0);
            savedTask.setSprint(null);
            return savedTask;
        });

        // Act
        TaskDTO result = taskService.removeTaskFromSprint(taskId, userId);

        // Assert
        assertNotNull(result);
        verify(taskRepository).findById(taskId);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void addTaskToEpic_ShouldAddTaskToEpic() {
        // Arrange
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(epicRepository.findById(epicId)).thenReturn(Optional.of(epic));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task savedTask = invocation.getArgument(0);
            savedTask.setEpic(epic);
            return savedTask;
        });

        // Act
        TaskDTO result = taskService.addTaskToEpic(taskId, epicId, userId);

        // Assert
        assertNotNull(result);
        verify(taskRepository).findById(taskId);
        verify(epicRepository).findById(epicId);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void removeTaskFromEpic_ShouldRemoveTaskFromEpic() {
        // Arrange
        task.setEpic(epic);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task savedTask = invocation.getArgument(0);
            savedTask.setEpic(null);
            return savedTask;
        });

        // Act
        TaskDTO result = taskService.removeTaskFromEpic(taskId, userId);

        // Assert
        assertNotNull(result);
        verify(taskRepository).findById(taskId);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void deleteTask_WithValidId_ShouldDeleteTask() {
        // Arrange
        when(taskRepository.existsById(taskId)).thenReturn(true);
        doNothing().when(taskRepository).deleteById(taskId);

        // Act
        taskService.deleteTask(taskId, userId);

        // Assert
        verify(taskRepository).existsById(taskId);
        verify(taskRepository).deleteById(taskId);
    }

    @Test
    void deleteTask_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(taskRepository.existsById(taskId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFound.class, () -> taskService.deleteTask(taskId, userId));
        verify(taskRepository).existsById(taskId);
        verify(taskRepository, never()).deleteById(any());
    }
}