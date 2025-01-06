package com.queryapplication.service.impl;

import com.queryapplication.dto.ActivityLogDTO;
import com.queryapplication.entity.ActivityLog;
import com.queryapplication.entity.Users;
import com.queryapplication.repository.ActivityLogRepository;
import com.queryapplication.service.ActivityLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActivityLogServiceImpl implements ActivityLogService {

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Override
    public void logActivity(Users user, String action, String target) {
        ActivityLog log = new ActivityLog();
        log.setUser(user);
        log.setAction(action);
        log.setTarget(target);

        activityLogRepository.save(log);
    }

    @Override
    public List<ActivityLogDTO> getAllLogs() {
        return activityLogRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ActivityLogDTO> getLogsByUser(Long userId) {
        return activityLogRepository.findByUserId(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ActivityLogDTO convertToDTO(ActivityLog log) {
        return new ActivityLogDTO(
                log.getCreatedAt().toLocalDate().format(DateTimeFormatter.ISO_DATE),
                log.getCreatedAt().toLocalTime().toString(),
                String.format("%s %s %s",
                        log.getUser().getFirstName(),
                        log.getAction(),
                        log.getTarget())
        );
    }
}