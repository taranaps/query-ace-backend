package com.queryapplication.dto;

import com.queryapplication.entity.Users;
import lombok.Data;

@Data
public class ActivityLogDTO {
    private Long userId; // User ID who performed the action
    private String action; // E.g., "Added Query", "Deleted User"
    private Integer targetId; // Target entity ID
    private String details; // Additional details about the action
}
