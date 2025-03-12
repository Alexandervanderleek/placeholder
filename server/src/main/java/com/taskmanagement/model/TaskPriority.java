package com.taskmanagement.model;

import jakarta.persistence.*;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "task_priorities")
public class TaskPriority {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;


    @Column(nullable = false)
    private int value;


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}