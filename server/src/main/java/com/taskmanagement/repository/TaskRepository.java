package com.taskmanagement.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taskmanagement.model.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    // Query methods will be implemented later
}