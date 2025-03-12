package com.taskmanagement.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taskmanagement.model.TaskPriority;

@Repository
public interface TaskPriorityRepository extends JpaRepository<TaskPriority, UUID> {
    // Query methods will be implemented later
}