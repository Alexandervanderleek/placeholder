package com.taskmanagement.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "task_comments")
public class TaskComment {
    @Id
    private UUID id;
}
