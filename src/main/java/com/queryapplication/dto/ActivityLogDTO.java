package com.queryapplication.dto;

import lombok.Data;
import java.time.LocalTime;

@Data
public class ActivityLogDTO {
    private String time;
    private String description;

    public ActivityLogDTO(LocalTime time, String description) {
        this.time = time.toString();
        this.description = description;
    }
}