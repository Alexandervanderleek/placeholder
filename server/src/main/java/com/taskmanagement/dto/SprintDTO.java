package com.taskmanagement.dto;

import java.time.ZonedDateTime;
import java.util.UUID;

public class SprintDTO {
    private UUID id;
    private String name;
    private String goal;
    private UUID scrumMasterId;
    private int capacityPoints;
    private ZonedDateTime startDate;
    private ZonedDateTime endDate;
    private boolean isActive;
}
