package com.taskmanagement.dto;

import java.util.UUID;

public class TaskFilterDTO {
    private UUID assignedToId;
    private UUID statusId;
    private UUID priorityId;
    private UUID sprintId;
    private UUID epicId;

    public TaskFilterDTO() {
    }

    public TaskFilterDTO(UUID assignedToId, UUID statusId, UUID priorityId, UUID sprintId, UUID epicId) {
        this.assignedToId = assignedToId;
        this.statusId = statusId;
        this.priorityId = priorityId;
        this.sprintId = sprintId;
        this.epicId = epicId;
    }

    public UUID getAssignedToId() {
        return assignedToId;
    }

    public void setAssignedToId(UUID assignedToId) {
        this.assignedToId = assignedToId;
    }

    public UUID getStatusId() {
        return statusId;
    }

    public void setStatusId(UUID statusId) {
        this.statusId = statusId;
    }

    public UUID getPriorityId() {
        return priorityId;
    }

    public void setPriorityId(UUID priorityId) {
        this.priorityId = priorityId;
    }

    public UUID getSprintId() {
        return sprintId;
    }

    public void setSprintId(UUID sprintId) {
        this.sprintId = sprintId;
    }

    public UUID getEpicId() {
        return epicId;
    }

    public void setEpicId(UUID epicId) {
        this.epicId = epicId;
    }
}