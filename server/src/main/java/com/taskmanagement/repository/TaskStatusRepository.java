package com.taskmanagement.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taskmanagement.model.TaskStatus;

@Repository
public interface TaskStatusRepository extends JpaRepository<TaskStatus, UUID> {
    // Query methods will be implemented later
}