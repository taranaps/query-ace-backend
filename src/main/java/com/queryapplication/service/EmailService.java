package com.queryapplication.service;

public interface EmailService {
    void sendPasswordResetEmail(String toEmail, String resetLink);
}
