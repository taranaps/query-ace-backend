package com.queryapplication.dto;
import lombok.Data;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Data
public class ActivityLogDTO {
    private String time;
    private String description;

    public ActivityLogDTO(LocalTime time, String description) {
        this.time = time.format(DateTimeFormatter.ofPattern("hh:mm a"));
        this.description = description;
    }
}