package com.queryapplication.service;

import com.queryapplication.entity.Users;

public interface ActivityLogService {
    void logActivity(Users user, String action, String details);
}
