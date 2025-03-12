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
            @ShellOption(value = {"-due", "--due-date"}, help = "Due date (YYYY-MM-DD)") String dueDate
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
            task.put("storyPoints", 0);
            task.put("estimatedHours", 0);

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
            @ShellOption(value = {"-due", "--due-date"}, help = "Due date (YYYY-MM-DD)", defaultValue = ShellOption.NULL) String dueDate
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

            apiService.put("/tasks/" + taskId, updatedTask, Object.class);
            shellService.printSuccess("Task updated successfully!");

        } catch (Exception e) {
            shellService.printError("Error updating task: " + e.getMessage());
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