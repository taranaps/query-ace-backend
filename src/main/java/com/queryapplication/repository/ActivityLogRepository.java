package com.queryapplication.repository;

import com.queryapplication.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findByUserId(Long userId); // Logs for a specific user
    List<ActivityLog> findByActionContaining(String action); // Search logs by action
}
