package com.queryapplication.repository;

import com.queryapplication.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    @Query("SELECT a FROM ActivityLog a WHERE a.performedByUser IS NOT NULL ORDER BY a.createdAt DESC")
    List<ActivityLog> findAllValidLogsOrderByCreatedAtDesc();
    List<ActivityLog> findByPerformedByUserIdOrderByCreatedAtDesc(Long userId);
}
