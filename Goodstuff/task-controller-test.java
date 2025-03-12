package com.taskmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanagement.dto.TaskDTO;
import com.taskmanagement.dto.TaskFilterDTO;
import com.taskmanagement.security.JWTTokenProvider;
import com.taskmanagement.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @MockBean
    private JWTTokenProvider tokenProvider;

    private UUID userId;
    private UUID taskId;
    private TaskDTO taskDTO;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        taskId = UUID.randomUUID();

        // Setup task DTO
        taskDTO = new TaskDTO();
        taskDTO.setId(taskId);
        taskDTO.setTitle("Test Task");
        taskDTO.setDescription("Test Description");
        taskDTO.setAssignedToId(userId);
        taskDTO.setAssignedToName("Test User");
        taskDTO.setStatusId(UUID.randomUUID());
        taskDTO.setStatusName("TODO");
        taskDTO.setPriorityId(UUID.randomUUID());
        taskDTO.setPriorityName("MEDIUM");
        taskDTO.setStoryPoints(3);
        taskDTO.setEstimatedHours(8);
        taskDTO.setDueDate(ZonedDateTime.now().plusDays(7));
        taskDTO.setCreatedById(userId);

        // Setup Spring Security user
        userDetails = new User(
                userId.toString(),
                "",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_DEVELOPER"))
        );
    }

    @Test
    @WithMockUser(username = "uuid-string", roles = "DEVELOPER")
    void getAllTasks_ShouldReturnAllTasks() throws Exception {
        // Arrange
        when(taskService.getAllTasks(any(UUID.class))).thenReturn(Arrays.asList(taskDTO));

        // Act & Assert
        mockMvc.perform(get("/api/tasks")
                .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(taskId.toString())))
                .andExpect(jsonPath("$[0].title", is("Test Task")));

        verify(taskService).getAllTasks(any(UUID.class));
    }

    @Test
    @WithMockUser(username = "uuid-string", roles = "DEVELOPER")
    void getTaskById_ShouldReturnTask() throws Exception {
        // Arrange
        when(taskService.getTaskById(eq(taskId), any(UUID.class))).thenReturn(taskDTO);

        // Act & Assert
        mockMvc.perform(get("/api/tasks/{id}", taskId)
                .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(taskId.toString())))
                .andExpect(jsonPath("$.title", is("Test Task")));

        verify(taskService).getTaskById(eq(taskId), any(UUID.class));
    }

    @Test
    @WithMockUser(username = "uuid-string", roles = "DEVELOPER")
    void createTask_ShouldCreateAndReturnTask() throws Exception {
        // Arrange
        when(taskService.createTask(any(TaskDTO.class), any(UUID.class))).thenReturn(taskDTO);

        // Act & Assert
        mockMvc.perform(post("/api/tasks")
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(taskId.toString())))
                .andExpect(jsonPath("$.title", is("Test Task")));

        verify(taskService).createTask(any(TaskDTO.class), any(UUID.class));
    }

    @Test
    @WithMockUser(username = "uuid-string", roles = "DEVELOPER")
    void updateTask_ShouldUpdateAndReturnTask() throws Exception {
        // Arrange
        when(taskService.updateTask(any(TaskDTO.class), any(UUID.class))).thenReturn(taskDTO);

        // Act & Assert
        mockMvc.perform(put("/api/tasks/{id}", taskId)
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(taskId.toString())))
                .andExpect(jsonPath("$.title", is("Test Task")));

        verify(taskService).updateTask(any(TaskDTO.class), any(UUID.class));
    }

    @Test
    @WithMockUser(username = "uuid-string", roles = "DEVELOPER")
    void deleteTask_ShouldDeleteTask() throws Exception {
        // Arrange
        doNothing().when(taskService).deleteTask(eq(taskId), any(UUID.class));

        // Act & Assert
        mockMvc.perform(delete("/api/tasks/{id}", taskId)
                .with(user(userDetails)))
                .andExpect(status().isNoContent());

        verify(taskService).deleteTask(eq(taskId), any(UUID.class));
    }

    @Test
    @WithMockUser(username = "uuid-string", roles = "DEVELOPER")
    void getTasksByFilter_ShouldReturnFilteredTasks() throws Exception {
        // Arrange
        TaskFilterDTO filterDTO = new TaskFilterDTO();
        filterDTO.setAssignedToId(userId);
        
        when(taskService.getTasksByFilter(any(TaskFilterDTO.class), any(UUID.class)))
                .thenReturn(Arrays.asList(taskDTO));

        // Act & Assert
        mockMvc.perform(post("/api/tasks/filter")
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filterDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(taskId.toString())));

        verify(taskService).getTasksByFilter(any(TaskFilterDTO.class), any(UUID.class));
    }

    @Test
    @WithMockUser(username = "uuid-string", roles = "DEVELOPER")
    void getMyActiveTasks_ShouldReturnUserActiveTasks() throws Exception {
        // Arrange
        when(taskService.getUserActiveTasks(any(UUID.class))).thenReturn(Arrays.asList(taskDTO));

        // Act & Assert
        mockMvc.perform(get("/api/tasks/my-tasks")
                .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(taskId.toString())));

        verify(taskService).getUserActiveTasks(any(UUID.class));
    }

    @Test
    @WithMockUser(username = "uuid-string", roles = "DEVELOPER")
    void changeTaskStatus_ShouldUpdateTaskStatus() throws Exception {
        // Arrange
        UUID newStatusId = UUID.randomUUID();
        TaskDTO updatedTask = taskDTO;
        updatedTask.setStatusId(newStatusId);
        updatedTask.setStatusName("IN_PROGRESS");
        
        when(taskService.changeTaskStatus(eq(taskId), eq(newStatusId), any(UUID.class)))
                .thenReturn(updatedTask);

        // Act & Assert
        mockMvc.perform(patch("/api/tasks/{id}/status/{statusId}", taskId, newStatusId)
                .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(taskId.toString())))
                .andExpect(jsonPath("$.statusName", is("IN_PROGRESS")));

        verify(taskService).changeTaskStatus(eq(taskId), eq(newStatusId), any(UUID.class));
    }

    @Test
    @WithMockUser(username = "uuid-string", roles = "DEVELOPER")
    void assignTask_ShouldUpdateTaskAssignee() throws Exception {
        // Arrange
        UUID newAssigneeId = UUID.randomUUID();
        TaskDTO updatedTask = taskDTO;
        updatedTask.setAssignedToId(newAssigneeId);
        updatedTask.setAssignedToName("New Assignee");
        
        when(taskService.assignTask(eq(taskId), eq(newAssigneeId), any(UUID.class)))
                .thenReturn(updatedTask);

        // Act & Assert
        mockMvc.perform(patch("/api/tasks/{id}/assign/{assigneeId}", taskId, newAssigneeId)
                .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(taskId.toString())))
                .andExpect(jsonPath("$.assignedToId", is(newAssigneeId.toString())));

        verify(taskService).assignTask(eq(taskId), eq(newAssigneeId), any(UUID.class));
    }
}