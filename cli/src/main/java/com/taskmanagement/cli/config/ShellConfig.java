package com.taskmanagement.cli.config;

import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.jline.PromptProvider;

@Configuration
public class ShellConfig {

    @Autowired
    private UserSession userSession;

    @Bean
    public PromptProvider promptProvider() {
        return () -> {
            String prompt = "task-cli";

            // Show username in prompt when logged in
            if (userSession.isAuthenticated()) {
                prompt += ":" + userSession.getUserName().split(" ")[0].toLowerCase();
            }

            return new AttributedString(
                    prompt + "> ",
                    AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN)
            );
        };
    }
}