package com.queryapplication.service.impl;

import com.queryapplication.entity.ActivityLog;
import com.queryapplication.entity.Users;
import com.queryapplication.repository.ActivityLogRepository;
import com.queryapplication.service.ActivityLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActivityLogServiceImpl implements ActivityLogService {

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Override
    public void logActivity(Users user, String action, String target) {
        // Create the log description
        String logDescription = String.format("%s %s %s", user.getFirstName(), action, target);

        // Create the ActivityLog object
        ActivityLog activityLog = new ActivityLog();
        activityLog.setUser(user);
        activityLog.setAction(action);
        activityLog.setTarget(target);

        // Save the activity log to the database
        activityLogRepository.save(activityLog);
    }
}
