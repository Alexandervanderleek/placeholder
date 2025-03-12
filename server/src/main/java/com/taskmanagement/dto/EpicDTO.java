package com.taskmanagement.dto;

import java.time.ZonedDateTime;
import java.util.UUID;

public class EpicDTO {
    private UUID id;
    private String name;
    private String description;
    private UUID ownerId;
    private int storyPoints;
    private ZonedDateTime startDate;
    private ZonedDateTime targetEndDate;
}
