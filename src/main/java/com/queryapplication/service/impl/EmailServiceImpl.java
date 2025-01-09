package com.queryapplication.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import com.queryapplication.service.EmailService;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender emailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Autowired
    public EmailServiceImpl(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    // Method to send the password reset email
    @Override
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        // Create a new email message
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);  // Sender email
        message.setTo(toEmail);  // Recipient email
        message.setSubject("Password Reset Request");
        message.setText("To reset your password, please click the link below:\n" + resetLink);

        // Send the email
        emailSender.send(message);
    }
}
