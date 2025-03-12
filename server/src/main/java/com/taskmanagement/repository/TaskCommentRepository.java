package com.taskmanagement.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taskmanagement.model.TaskComment;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, UUID> {
    // Query methods will be implemented later
}