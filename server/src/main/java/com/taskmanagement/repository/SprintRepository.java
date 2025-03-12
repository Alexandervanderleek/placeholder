package com.taskmanagement.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taskmanagement.model.Sprint;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, UUID> {
    // Query methods will be implemented later
}