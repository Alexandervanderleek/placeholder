package com.taskmanagement.cli.command;

import com.taskmanagement.cli.config.UserSession;
import com.taskmanagement.cli.service.APIService;
import com.taskmanagement.cli.service.ShellService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ShellComponent
public class TaskShellCommand {

    @Autowired
    private APIService apiService;

    @Autowired
    private UserSession userSession;

    @Autowired
    private ShellService shellService;

    @ShellMethod(key = "task-list", value = "List all tasks")
    @ShellMethodAvailability("isUserLoggedIn")
    public void listTasks() {
        try {
            shellService.printHeading("Fetching Tasks...");

            Object[] tasks = apiService.get("/tasks", Object[].class);
            if (tasks.length == 0) {
                shellService.printInfo("No tasks found");
            } else {
                List<String[]> tableData = new ArrayList<>();

                for (Object taskObj : tasks) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> task = (Map<String, Object>) taskObj;

                    String[] row = new String[5];
                    row[0] = String.valueOf(task.get("id"));
                    row[1] = String.valueOf(task.get("title"));
                    row[2] = String.valueOf(task.get("assignedToName"));
                    row[3] = String.valueOf(task.get("statusName"));
                    row[4] = String.valueOf(task.get("priorityName"));

                    tableData.add(row);
                }

                String[] headers = {"ID", "Title", "Assigned To", "Status", "Priority"};
                shellService.printTable(headers, tableData.toArray(new String[0][]));
            }
        } catch (Exception e) {
            shellService.printError("Error fetching tasks: " + e.getMessage());
        }
    }

    @ShellMethod(key = "task-create", value = "Create a new task")
    @ShellMethodAvailability("isUserLoggedIn")
    public void createTask(
            @ShellOption(value = {"-t", "--title"}, help = "Task title") String title,
            @ShellOption(value = {"-d", "--desc"}, help = "Task description") String description,
            @ShellOption(value = {"-a", "--assignee"}, help = "Assignee ID") String assigneeId,
            @ShellOption(value = {"-s", "--status"}, help = "Status ID") String statusId,
            @ShellOption(value = {"-p", "--priority"}, help = "Priority ID") String priorityId,
            @ShellOption(value = {"-due", "--due-date"}, help = "Due date (YYYY-MM-DD)") String dueDate,
            @ShellOption(value = {"-e", "--epic"}, help = "Epic ID", defaultValue = ShellOption.NULL) String epicId,
            @ShellOption(value = {"-sp", "--sprint"}, help = "Sprint ID", defaultValue = ShellOption.NULL) String sprintId,
            @ShellOption(value = {"-pts", "--story-points"}, help = "Story points", defaultValue = "0") Integer storyPoints,
            @ShellOption(value = {"-hrs", "--estimated-hours"}, help = "Estimated hours", defaultValue = "0") Integer estimatedHours
    ) {
        try {
            shellService.printHeading("Creating new task...");

            // Create task request object
            Map<String, Object> task = new HashMap<>();
            task.put("title", title);
            task.put("description", description);
            task.put("assignedToId", assigneeId);
            task.put("statusId", statusId);
            task.put("priorityId", priorityId);
            task.put("dueDate", dueDate);
            task.put("storyPoints", storyPoints);
            task.put("estimatedHours", estimatedHours);
            
            if (epicId != null) {
                task.put("epicId", epicId);
            }
            
            if (sprintId != null) {
                task.put("sprintId", sprintId);
            }

            Object createdTask = apiService.post("/tasks", task, Object.class);
            shellService.printSuccess("Task created successfully!");

            // Display the created task
            @SuppressWarnings("unchecked")
            Map<String, Object> taskResult = (Map<String, Object>) createdTask;
            shellService.printInfo("ID: " + taskResult.get("id"));
            shellService.printInfo("Title: " + taskResult.get("title"));

        } catch (Exception e) {
            shellService.printError("Error creating task: " + e.getMessage());
        }
    }

    @ShellMethod(key = "task-get", value = "Get task details")
    @ShellMethodAvailability("isUserLoggedIn")
    public void getTask(@ShellOption(help = "Task ID") String taskId) {
        try {
            shellService.printHeading("Fetching task details...");

            Object taskObj = apiService.get("/tasks/" + taskId, Object.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> task = (Map<String, Object>) taskObj;

            shellService.printHeading("Task Details:");
            shellService.printInfo("ID: " + task.get("id"));
            shellService.printInfo("Title: " + task.get("title"));
            shellService.printInfo("Description: " + task.get("description"));
            shellService.printInfo("Assigned to: " + task.get("assignedToName"));
            shellService.printInfo("Status: " + task.get("statusName"));
            shellService.printInfo("Priority: " + task.get("priorityName"));
            shellService.printInfo("Story Points: " + task.get("storyPoints"));
            shellService.printInfo("Estimated Hours: " + task.get("estimatedHours"));
            shellService.printInfo("Due Date: " + task.get("dueDate"));
            
            if (task.get("epicName") != null) {
                shellService.printInfo("Epic: " + task.get("epicName") + " (" + task.get("epicId") + ")");
            }
            
            if (task.get("sprintName") != null) {
                shellService.printInfo("Sprint: " + task.get("sprintName") + " (" + task.get("sprintId") + ")");
            }
        } catch (Exception e) {
            shellService.printError("Error fetching task: " + e.getMessage());
        }
    }

    @ShellMethod(key = "task-update", value = "Update a task")
    @ShellMethodAvailability("isUserLoggedIn")
    public void updateTask(
            @ShellOption(help = "Task ID") String taskId,
            @ShellOption(value = {"-t", "--title"}, help = "Task title", defaultValue = ShellOption.NULL) String title,
            @ShellOption(value = {"-d", "--desc"}, help = "Task description", defaultValue = ShellOption.NULL) String description,
            @ShellOption(value = {"-a", "--assignee"}, help = "Assignee ID", defaultValue = ShellOption.NULL) String assigneeId,
            @ShellOption(value = {"-s", "--status"}, help = "Status ID", defaultValue = ShellOption.NULL) String statusId,
            @ShellOption(value = {"-p", "--priority"}, help = "Priority ID", defaultValue = ShellOption.NULL) String priorityId,
            @ShellOption(value = {"-due", "--due-date"}, help = "Due date (YYYY-MM-DD)", defaultValue = ShellOption.NULL) String dueDate,
            @ShellOption(value = {"-e", "--epic"}, help = "Epic ID", defaultValue = ShellOption.NULL) String epicId,
            @ShellOption(value = {"-sp", "--sprint"}, help = "Sprint ID", defaultValue = ShellOption.NULL) String sprintId,
            @ShellOption(value = {"-pts", "--story-points"}, help = "Story points", defaultValue = ShellOption.NULL) Integer storyPoints,
            @ShellOption(value = {"-hrs", "--estimated-hours"}, help = "Estimated hours", defaultValue = ShellOption.NULL) Integer estimatedHours
    ) {
        try {
            shellService.printHeading("Updating task...");

            // First get the current task
            Object currentTaskObj = apiService.get("/tasks/" + taskId, Object.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> currentTask = (Map<String, Object>) currentTaskObj;

            // Update only the fields that were provided
            Map<String, Object> updatedTask = new HashMap<>(currentTask);
            if (title != null) updatedTask.put("title", title);
            if (description != null) updatedTask.put("description", description);
            if (assigneeId != null) updatedTask.put("assignedToId", assigneeId);
            if (statusId != null) updatedTask.put("statusId", statusId);
            if (priorityId != null) updatedTask.put("priorityId", priorityId);
            if (dueDate != null) updatedTask.put("dueDate", dueDate);
            if (epicId != null) updatedTask.put("epicId", epicId);
            if (sprintId != null) updatedTask.put("sprintId", sprintId);
            if (storyPoints != null) updatedTask.put("storyPoints", storyPoints);
            if (estimatedHours != null) updatedTask.put("estimatedHours", estimatedHours);

            apiService.put("/tasks/" + taskId, updatedTask, Object.class);
            shellService.printSuccess("Task updated successfully!");

        } catch (Exception e) {
            shellService.printError("Error updating task: " + e.getMessage());
        }
    }

    @ShellMethod(key = "task-change-status", value = "Change task status")
    @ShellMethodAvailability("isUserLoggedIn")
    public void changeTaskStatus(
            @ShellOption(help = "Task ID") String taskId,
            @ShellOption(help = "Status ID") String statusId
    ) {
        try {
            shellService.printHeading("Changing task status...");
            
            Object updatedTask = apiService.patch("/tasks/" + taskId + "/status/" + statusId, null, Object.class);
            shellService.printSuccess("Task status changed successfully!");
            
            @SuppressWarnings("unchecked")
            Map<String, Object> taskResult = (Map<String, Object>) updatedTask;
            shellService.printInfo("New Status: " + taskResult.get("statusName"));
            
        } catch (Exception e) {
            shellService.printError("Error changing task status: " + e.getMessage());
        }
    }
    
    @ShellMethod(key = "task-assign", value = "Assign task to user")
    @ShellMethodAvailability("isUserLoggedIn")
    public void assignTask(
            @ShellOption(help = "Task ID") String taskId,
            @ShellOption(help = "Assignee ID") String assigneeId
    ) {
        try {
            shellService.printHeading("Assigning task...");
            
            Object updatedTask = apiService.patch("/tasks/" + taskId + "/assign/" + assigneeId, null, Object.class);
            shellService.printSuccess("Task assigned successfully!");
            
            @SuppressWarnings("unchecked")
            Map<String, Object> taskResult = (Map<String, Object>) updatedTask;
            shellService.printInfo("Assigned to: " + taskResult.get("assignedToName"));
            
        } catch (Exception e) {
            shellService.printError("Error assigning task: " + e.getMessage());
        }
    }
    
    @ShellMethod(key = "task-add-to-sprint", value = "Add task to sprint")
    @ShellMethodAvailability("isUserLoggedIn")
    public void addTaskToSprint(
            @ShellOption(help = "Task ID") String taskId,
            @ShellOption(help = "Sprint ID") String sprintId
    ) {
        try {
            shellService.printHeading("Adding task to sprint...");
            
            Object updatedTask = apiService.patch("/tasks/" + taskId + "/add-to-sprint/" + sprintId, null, Object.class);
            shellService.printSuccess("Task added to sprint successfully!");
            
            @SuppressWarnings("unchecked")
            Map<String, Object> taskResult = (Map<String, Object>) updatedTask;
            shellService.printInfo("Added to sprint: " + taskResult.get("sprintName"));
            
        } catch (Exception e) {
            shellService.printError("Error adding task to sprint: " + e.getMessage());
        }
    }
    
    @ShellMethod(key = "task-remove-from-sprint", value = "Remove task from sprint")
    @ShellMethodAvailability("isUserLoggedIn")
    public void removeTaskFromSprint(
            @ShellOption(help = "Task ID") String taskId
    ) {
        try {
            shellService.printHeading("Removing task from sprint...");
            
            Object updatedTask = apiService.patch("/tasks/" + taskId + "/remove-from-sprint", null, Object.class);
            shellService.printSuccess("Task removed from sprint successfully!");
            
        } catch (Exception e) {
            shellService.printError("Error removing task from sprint: " + e.getMessage());
        }
    }
    
    @ShellMethod(key = "task-add-to-epic", value = "Add task to epic")
    @ShellMethodAvailability("isUserLoggedIn")
    public void addTaskToEpic(
            @ShellOption(help = "Task ID") String taskId,
            @ShellOption(help = "Epic ID") String epicId
    ) {
        try {
            shellService.printHeading("Adding task to epic...");
            
            Object updatedTask = apiService.patch("/tasks/" + taskId + "/add-to-epic/" + epicId, null, Object.class);
            shellService.printSuccess("Task added to epic successfully!");
            
            @SuppressWarnings("unchecked")
            Map<String, Object> taskResult = (Map<String, Object>) updatedTask;
            shellService.printInfo("Added to epic: " + taskResult.get("epicName"));
            
        } catch (Exception e) {
            shellService.printError("Error adding task to epic: " + e.getMessage());
        }
    }
    
    @ShellMethod(key = "task-remove-from-epic", value = "Remove task from epic")
    @ShellMethodAvailability("isUserLoggedIn")
    public void removeTaskFromEpic(
            @ShellOption(help = "Task ID") String taskId
    ) {
        try {
            shellService.printHeading("Removing task from epic...");
            
            Object updatedTask = apiService.patch("/tasks/" + taskId + "/remove-from-epic", null, Object.class);
            shellService.printSuccess("Task removed from epic successfully!");
            
        } catch (Exception e) {
            shellService.printError("Error removing task from epic: " + e.getMessage());
        }
    }
    
    @ShellMethod(key = "task-filter", value = "Filter tasks")
    @ShellMethodAvailability("isUserLoggedIn")
    public void filterTasks(
            @ShellOption(value = {"-a", "--assignee"}, help = "Assignee ID", defaultValue = ShellOption.NULL) String assigneeId,
            @ShellOption(value = {"-s", "--status"}, help = "Status ID", defaultValue = ShellOption.NULL) String statusId,
            @ShellOption(value = {"-p", "--priority"}, help = "Priority ID", defaultValue = ShellOption.NULL) String priorityId,
            @ShellOption(value = {"-sp", "--sprint"}, help = "Sprint ID", defaultValue = ShellOption.NULL) String sprintId,
            @ShellOption(value = {"-e", "--epic"}, help = "Epic ID", defaultValue = ShellOption.NULL) String epicId
    ) {
        try {
            shellService.printHeading("Filtering tasks...");
            
            Map<String, Object> filterParams = new HashMap<>();
            if (assigneeId != null) filterParams.put("assignedToId", assigneeId);
            if (statusId != null) filterParams.put("statusId", statusId);
            if (priorityId != null) filterParams.put("priorityId", priorityId);
            if (sprintId != null) filterParams.put("sprintId", sprintId);
            if (epicId != null) filterParams.put("epicId", epicId);
            
            Object[] tasks = apiService.post("/tasks/filter", filterParams, Object[].class);
            
            if (tasks.length == 0) {
                shellService.printInfo("No tasks found matching the filter criteria");
            } else {
                List<String[]> tableData = new ArrayList<>();
                
                for (Object taskObj : tasks) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> task = (Map<String, Object>) taskObj;
                    
                    String[] row = new String[5];
                    row[0] = String.valueOf(task.get("id"));
                    row[1] = String.valueOf(task.get("title"));
                    row[2] = String.valueOf(task.get("assignedToName"));
                    row[3] = String.valueOf(task.get("statusName"));
                    row[4] = String.valueOf(task.get("priorityName"));
                    
                    tableData.add(row);
                }
                
                String[] headers = {"ID", "Title", "Assigned To", "Status", "Priority"};
                shellService.printTable(headers, tableData.toArray(new String[0][]));
            }
            
        } catch (Exception e) {
            shellService.printError("Error filtering tasks: " + e.getMessage());
        }
    }
    
    @ShellMethod(key = "task-my", value = "List my active tasks")
    @ShellMethodAvailability("isUserLoggedIn")
    public void getMyTasks() {
        try {
            shellService.printHeading("Fetching your active tasks...");
            
            Object[] tasks = apiService.get("/tasks/my-tasks", Object[].class);
            
            if (tasks.length == 0) {
                shellService.printInfo("You have no active tasks");
            } else {
                List<String[]> tableData = new ArrayList<>();
                
                for (Object taskObj : tasks) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> task = (Map<String, Object>) taskObj;
                    
                    String[] row = new String[4];
                    row[0] = String.valueOf(task.get("id"));
                    row[1] = String.valueOf(task.get("title"));
                    row[2] = String.valueOf(task.get("statusName"));
                    row[3] = String.valueOf(task.get("priorityName"));
                    
                    tableData.add(row);
                }
                
                String[] headers = {"ID", "Title", "Status", "Priority"};
                shellService.printTable(headers, tableData.toArray(new String[0][]));
            }
            
        } catch (Exception e) {
            shellService.printError("Error fetching your tasks: " + e.getMessage());
        }
    }

    @ShellMethod(key = "task-delete", value = "Delete a task")
    @ShellMethodAvailability("isUserLoggedIn")
    public void deleteTask(@ShellOption(help = "Task ID") String taskId) {
        try {
            shellService.printHeading("Deleting task...");
            apiService.delete("/tasks/" + taskId, Object.class);
            shellService.printSuccess("Task deleted successfully!");
        } catch (Exception e) {
            shellService.printError("Error deleting task: " + e.getMessage());
        }
    }

    public Availability isUserLoggedIn() {
        return userSession.isAuthenticated()
                ? Availability.available()
                : Availability.unavailable("you are not logged in. Please use 'login' command first");
    }
}