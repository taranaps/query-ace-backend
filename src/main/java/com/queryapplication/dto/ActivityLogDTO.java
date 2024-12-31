package com.queryapplication.dto;

import com.queryapplication.entity.Users;
import lombok.Data;

@Data
public class ActivityLogDTO {
    private Long userId;
    private String action;
    private String details;
}
