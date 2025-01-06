package com.queryapplication.controller;

import com.queryapplication.dto.ActivityLogDTO;
import com.queryapplication.service.ActivityLogService;
import com.queryapplication.entity.ActivityLog;
import com.queryapplication.repository.ActivityLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/activity-logs")
public class ActivityLogController {

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private ActivityLogService activityLogService;

    @GetMapping("/all")
    public List<ActivityLogDTO> getAllLogs() {
        List<ActivityLog> logs = activityLogRepository.findAllByOrderByCreatedAtDesc();

        return logs.stream()
                .collect(Collectors.groupingBy(log -> log.getCreatedAt().toLocalDate()))
                .entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .map(log -> new ActivityLogDTO(
                                entry.getKey().format(DateTimeFormatter.ISO_DATE), // Log Date
                                log.getCreatedAt().toLocalTime().toString(), // Timestamp
                                String.format("%s %s %s", log.getUser().getFirstName(), log.getAction(), log.getTarget()) // Log description
                        )))
                .collect(Collectors.toList());
    }
}
