package com.taskmanagement.cli.command;

import com.taskmanagement.cli.config.UserSession;
import com.taskmanagement.cli.service.ShellService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@Component
public class WelcomeCommand {

    @Autowired
    private ShellService shellService;

    @Autowired
    private UserSession userSession;

    // This is no longer a shell command, just a regular method that can be called programmatically
    public void welcome() {
        try {
            // Read banner from resources
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new ClassPathResource("banner.txt").getInputStream()));
            String banner = reader.lines().collect(Collectors.joining("\n"));

            shellService.printInfo(banner);
            shellService.printHeading("\nWelcome to Task Management CLI!");

            if (userSession.isAuthenticated()) {
                shellService.printSuccess("You are logged in as: " + userSession.getUserName() +
                        " (" + userSession.getUserEmail() + ")");
                shellService.printInfo("Type 'task-list' to see your tasks or 'help' to see all available commands.");
            } else {
                shellService.printWarning("You are not logged in. Please use 'login' to authenticate.");
                shellService.printInfo("Type 'help' to see all available commands.");
            }
        } catch (Exception e) {
            // If banner can't be loaded, just show a simple message
            shellService.printHeading("\nWelcome to Task Management CLI!");

            if (userSession.isAuthenticated()) {
                shellService.printSuccess("You are logged in as: " + userSession.getUserName());
                shellService.printInfo("Type 'task-list' to see your tasks or 'help' to see all available commands.");
            } else {
                shellService.printWarning("You are not logged in. Please use 'login' to authenticate.");
                shellService.printInfo("Type 'help' to see all available commands.");
            }
        }
    }
}