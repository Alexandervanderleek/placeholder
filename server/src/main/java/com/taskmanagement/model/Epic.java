package com.taskmanagement.model;

import jakarta.persistence.*;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "epics")
public class Epic {
   @Id
   private UUID id;

   @Column(nullable = false)
   private String name;

   @Column(nullable = false)
   private String description;

   @ManyToOne(fetch = FetchType.EAGER)
   @JoinColumn(name = "owner_id", nullable = false)
   private User owner;

   @Column(name = "story_points", nullable = false)
   private int storyPoints;

   @Column(name = "start_date", nullable = false)
   private ZonedDateTime startDate;

   @Column(name = "target_end_date", nullable = false)
   private ZonedDateTime targetEndDate;

   @Column(name = "actual_end_date")
   private ZonedDateTime actualEndDate;

   @Column(name = "created_at", nullable = false)
   private ZonedDateTime createdAt;

   @Column(name = "updated_at", nullable = false)
   private ZonedDateTime updatedAt;

   @PrePersist
   protected void onCreate() {
      id = UUID.randomUUID();
      createdAt = updatedAt = ZonedDateTime.now();
   }

   @PreUpdate
   protected void onUpdate() {
      updatedAt = ZonedDateTime.now();
   }

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

   public String getDescription() {
      return description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public User getOwner() {
      return owner;
   }

   public void setOwner(User owner) {
      this.owner = owner;
   }

   public int getStoryPoints() {
      return storyPoints;
   }

   public void setStoryPoints(int storyPoints) {
      this.storyPoints = storyPoints;
   }

   public ZonedDateTime getStartDate() {
      return startDate;
   }

   public void setStartDate(ZonedDateTime startDate) {
      this.startDate = startDate;
   }

   public ZonedDateTime getTargetEndDate() {
      return targetEndDate;
   }

   public void setTargetEndDate(ZonedDateTime targetEndDate) {
      this.targetEndDate = targetEndDate;
   }

   public ZonedDateTime getActualEndDate() {
      return actualEndDate;
   }

   public void setActualEndDate(ZonedDateTime actualEndDate) {
      this.actualEndDate = actualEndDate;
   }

   public ZonedDateTime getCreatedAt() {
      return createdAt;
   }

   public void setCreatedAt(ZonedDateTime createdAt) {
      this.createdAt = createdAt;
   }

   public ZonedDateTime getUpdatedAt() {
      return updatedAt;
   }

   public void setUpdatedAt(ZonedDateTime updatedAt) {
      this.updatedAt = updatedAt;
   }
}