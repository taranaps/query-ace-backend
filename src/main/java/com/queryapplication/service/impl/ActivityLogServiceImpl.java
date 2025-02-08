package com.queryapplication.service.impl;

import com.queryapplication.dto.ActivityLogDTO;
import com.queryapplication.dto.DateGroupedLogsDTO;
import com.queryapplication.entity.ActivityLog;
import com.queryapplication.entity.Users;
import com.queryapplication.exception.ResourceNotFoundException;
import com.queryapplication.repository.ActivityLogRepository;
import com.queryapplication.repository.UserRepository;
import com.queryapplication.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;
    private static final int DATES_PER_PAGE = 10;

    private Users getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", 0L));
    }

    @Override
    @Transactional
    public void logActivity(String action, String description) {
        Users performedByUser = getLoggedInUser();

        ActivityLog log = new ActivityLog();
        log.setPerformedByUser(performedByUser);
        log.setAction(action);
        log.setDescription(description);
        activityLogRepository.save(log);
    }

    @Override
    public List<DateGroupedLogsDTO> getAllLogs(int page) {
        try {
            List<ActivityLog> allLogs = activityLogRepository.findAllValidLogsOrderByCreatedAtDesc();

            if (allLogs.isEmpty()) {
                throw new ResourceNotFoundException("Activity Logs", "records", 0);
            }

            Map<LocalDate, List<ActivityLog>> groupedByDate = allLogs.stream()
                    .collect(Collectors.groupingBy(
                            log -> log.getCreatedAt().toLocalDate(),
                            TreeMap::new,
                            Collectors.toList()
                    ));

            List<LocalDate> dates = new ArrayList<>(groupedByDate.keySet());
            int start = page * DATES_PER_PAGE;
            int end = Math.min(start + DATES_PER_PAGE, dates.size());

            if (start >= dates.size()) {
                throw new ResourceNotFoundException("Activity Logs", "page", page);
            }

            return dates.subList(start, end).stream()
                    .map(date -> new DateGroupedLogsDTO(
                            date.toString(),
                            groupedByDate.get(date).stream()
                                    .map(log -> new ActivityLogDTO(
                                            log.getCreatedAt().toLocalTime(),
                                            formatDescription(log)
                                    ))
                                    .collect(Collectors.toList())
                    ))
                    .collect(Collectors.toList());

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error accessing activity logs: " + e.getMessage());
        }
    }

    @Override
    public List<DateGroupedLogsDTO> getUserLogs(Long userId, int page) {
        try {
            Users user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

            List<ActivityLog> userLogs = activityLogRepository.findByPerformedByUserIdOrderByCreatedAtDesc(userId);

            if (userLogs.isEmpty()) {
                throw new ResourceNotFoundException("Activity Logs", "user id", userId);
            }

            return groupLogsByDate(userLogs);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error accessing user logs: " + e.getMessage());
        }
    }

    private String formatDescription(ActivityLog log) {
        return String.format("%s %s %s",
                log.getPerformedByUser().getUsername(),
                log.getAction().toLowerCase(),
                log.getDescription()
        );
    }

    private List<DateGroupedLogsDTO> groupLogsByDate(List<ActivityLog> logs) {
        Map<LocalDate, List<ActivityLogDTO>> groupedLogs = new TreeMap<>(Collections.reverseOrder());

        for (ActivityLog log : logs) {
            LocalDate date = log.getCreatedAt().toLocalDate();
            ActivityLogDTO logDTO = new ActivityLogDTO(
                    log.getCreatedAt().toLocalTime(),
                    formatDescription(log)
            );

            groupedLogs.computeIfAbsent(date, k -> new ArrayList<>()).add(logDTO);
        }

        return groupedLogs.entrySet().stream()
                .map(entry -> new DateGroupedLogsDTO(
                        entry.getKey().toString(),
                        entry.getValue()
                ))
                .collect(Collectors.toList());
    }
}