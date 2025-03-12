package com.taskmanagement.dto;

import java.util.UUID;

public class AuthResponseDTO {
    private String token;
    private UUID userId;
    private String name;
    private String email;

    public AuthResponseDTO() {
    }

    public AuthResponseDTO(String token, UUID userId, String name, String email) {
        this.token = token;
        this.userId = userId;
        this.name = name;
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}