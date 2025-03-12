package com.taskmanagement.cli.command;

import com.taskmanagement.cli.config.UserSession;
import com.taskmanagement.cli.service.ShellService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;

@ShellComponent
public class EpicSprintShellCommands {

    @Autowired
    private UserSession userSession;

    @Autowired
    private ShellService shellService;

    /* Epic commands */

    @ShellMethod(key = "epic-list", value = "List all epics")
    @ShellMethodAvailability("isUserLoggedIn")
    public void listEpics() {
        shellService.printInfo("Feature coming soon: List of epics will be displayed here.");
        shellService.printInfo("This feature is not yet implemented.");
    }

    @ShellMethod(key = "epic-create", value = "Create a new epic")
    @ShellMethodAvailability("isUserLoggedIn")
    public void createEpic() {
        shellService.printInfo("Feature coming soon: Create epic functionality will be added here.");
        shellService.printInfo("This feature is not yet implemented.");
    }

    /* Sprint commands */

    @ShellMethod(key = "sprint-list", value = "List all sprints")
    @ShellMethodAvailability("isUserLoggedIn")
    public void listSprints() {
        shellService.printInfo("Feature coming soon: List of sprints will be displayed here.");
        shellService.printInfo("This feature is not yet implemented.");
    }

    @ShellMethod(key = "sprint-create", value = "Create a new sprint")
    @ShellMethodAvailability("isUserLoggedIn")
    public void createSprint() {
        shellService.printInfo("Feature coming soon: Create sprint functionality will be added here.");
        shellService.printInfo("This feature is not yet implemented.");
    }

    @ShellMethod(key = "sprint-start", value = "Start a sprint")
    @ShellMethodAvailability("isUserLoggedIn")
    public void startSprint() {
        shellService.printInfo("Feature coming soon: Start sprint functionality will be added here.");
        shellService.printInfo("This feature is not yet implemented.");
    }

    @ShellMethod(key = "sprint-end", value = "End a sprint")
    @ShellMethodAvailability("isUserLoggedIn")
    public void endSprint() {
        shellService.printInfo("Feature coming soon: End sprint functionality will be added here.");
        shellService.printInfo("This feature is not yet implemented.");
    }

    public Availability isUserLoggedIn() {
        return userSession.isAuthenticated()
                ? Availability.available()
                : Availability.unavailable("you are not logged in. Please use 'login' command first");
    }
}