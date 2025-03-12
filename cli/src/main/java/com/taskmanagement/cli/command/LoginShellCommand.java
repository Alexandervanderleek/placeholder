package com.taskmanagement.cli.command;

import com.taskmanagement.cli.config.UserSession;
import com.taskmanagement.cli.service.APIService;
import com.taskmanagement.cli.service.OAuthService;
import com.taskmanagement.cli.service.ShellService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.awt.Desktop;
import java.net.URI;
import java.util.Map;
import java.util.Scanner;

@ShellComponent
public class LoginShellCommand {

    @Autowired
    private OAuthService oAuthService;

    @Autowired
    private APIService apiService;

    @Autowired
    private UserSession userSession;

    @Autowired
    private ShellService shellService;

    @ShellMethod(key = "signin", value = "Authenticate with Google")
    public void login(@ShellOption(value = {"--headless"}, help = "Run in headless mode (provide token manually)", defaultValue = "false") boolean headless) {
        try {
            shellService.printInfo("Starting authentication with Google...");

            String idToken;

            if (headless) {
                shellService.printInfo("Running in headless mode. Please authenticate in a browser and paste the ID token here:");
                Scanner scanner = new Scanner(System.in);
                idToken = scanner.nextLine().trim();
            } else {
                // Generate auth URL and open in browser
                URI authUrl = oAuthService.getAuthorizationUrl();

                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    shellService.printInfo("Opening browser for authentication...");
                    Desktop.getDesktop().browse(authUrl);
                } else {
                    shellService.printInfo("Please open the following URL in your browser to authenticate:");
                    shellService.printInfo(authUrl.toString());
                }

                // Start local server to receive the auth callback
                shellService.printInfo("Waiting for authentication...");
                idToken = oAuthService.waitForAuthorizationCode();

                if (idToken == null) {
                    shellService.printError("Failed to obtain authentication token. Please try again.");
                    return;
                }
            }

            // Exchange Google ID token for application JWT token
            shellService.printInfo("Authenticating with server...");

            try {
                Map<String, Object> response = apiService.authenticate(idToken);

                String token = (String) response.get("token");
                String name = (String) response.get("name");
                String email = (String) response.get("email");

                // Save token to session
                userSession.setToken(token);
                userSession.setUserName(name);
                userSession.setUserEmail(email);
                userSession.saveToFile();

                shellService.printSuccess("Successfully authenticated as " + name + " (" + email + ")");
            } catch (Exception e) {
                shellService.printError("Authentication failed: " + e.getMessage());
            }
        } catch (Exception e) {
            shellService.printError("Error during login: " + e.getMessage());
        }
    }

    @ShellMethod(key = "logout", value = "Log out and clear session")
    public void logout() {
        if (userSession.isAuthenticated()) {
            String name = userSession.getUserName();
            userSession.clearSession();
            shellService.printSuccess("Logged out successfully. Goodbye, " + name + "!");
        } else {
            shellService.printWarning("You are not currently logged in.");
        }
    }

    @ShellMethod(key = "whoami", value = "Show current user information")
    public void whoami() {
        if (userSession.isAuthenticated()) {
            shellService.printHeading("Current User Information");
            shellService.printInfo("Name: " + userSession.getUserName());
            shellService.printInfo("Email: " + userSession.getUserEmail());
        } else {
            shellService.printWarning("You are not currently logged in.");
        }
    }
}