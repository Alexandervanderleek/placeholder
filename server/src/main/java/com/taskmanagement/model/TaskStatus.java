package com.taskmanagement.model;

import jakarta.persistence.*;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "task_statuses")
public class TaskStatus {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;


    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    // Getters and Setters

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

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

}