package com.queryapplication.service;

import com.queryapplication.dto.ActivityLogDTO;
import com.queryapplication.entity.Users;
import java.util.List;

public interface ActivityLogService {
    // Core logging method
    void logActivity(Users user, String action, String target);

    // Get logs
    List<ActivityLogDTO> getAllLogs();
    List<ActivityLogDTO> getLogsByUser(Long userId);
}