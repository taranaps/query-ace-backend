package com.queryapplication.dto;

import lombok.Data;
import java.util.List;

@Data
public class DateGroupedLogsDTO {
    private String date;
    private List<ActivityLogDTO> logs;

    public DateGroupedLogsDTO(String date, List<ActivityLogDTO> logs) {
        this.date = date;
        this.logs = logs;
    }
}