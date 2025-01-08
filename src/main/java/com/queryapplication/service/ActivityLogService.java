package com.queryapplication.service;

import com.queryapplication.dto.DateGroupedLogsDTO;
import java.util.List;

public interface ActivityLogService {
    List<DateGroupedLogsDTO> getAllLogs(int page);
    List<DateGroupedLogsDTO> getUserLogs(Long userId, int page);
    void logActivity(String action, String description);
}