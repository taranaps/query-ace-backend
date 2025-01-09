package com.queryapplication.service.impl;

import com.queryapplication.dto.PasswordResetDTO;
import com.queryapplication.entity.PasswordChangeRequest;
import com.queryapplication.entity.Users;
import com.queryapplication.repository.PasswordChangeRequestRepository;
import com.queryapplication.repository.UserRepository;
import com.queryapplication.service.EmailService;
import com.queryapplication.service.PasswordService;
import com.queryapplication.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordServiceImpl implements PasswordService {


    private UserRepository userRepository;
    private PasswordChangeRequestRepository passwordChangeRequestRepository;

    private UsersService usersService;
    private EmailService emailService;

    @Autowired
    public PasswordServiceImpl(UserRepository userRepository, PasswordChangeRequestRepository passwordChangeRequestRepository, UsersService usersService, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordChangeRequestRepository = passwordChangeRequestRepository;
        this.usersService = usersService;
        this.emailService = emailService;
    }

    @Override
    public boolean initiatePasswordReset(String email) {
        Optional<Users> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return false; // User not found
        }

        Users user = userOptional.get();
        // Generate a unique hashId for the password reset request
        String hashId = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusHours(1);

        PasswordChangeRequest passwordChangeRequest = new PasswordChangeRequest();
        passwordChangeRequest.setHashId(hashId);
        passwordChangeRequest.setExpiry(expiry);
        passwordChangeRequest.setUser(user);

        passwordChangeRequestRepository.save(passwordChangeRequest);

        String resetLink = "http://localhost:8080/api/v1/queryapplication/auth/reset-password/" + hashId;
        emailService.sendPasswordResetEmail(user.getEmail(), resetLink);

        return true;
    }

    @Override
    public boolean validatePasswordReset(String hashId) {
        Optional<PasswordChangeRequest> requestOptional = passwordChangeRequestRepository.findByHashId(hashId);
        if (requestOptional.isEmpty()) {
            return false; // Invalid hashId
        }

        PasswordChangeRequest passwordChangeRequest = requestOptional.get();
        if (passwordChangeRequest.getExpiry().isBefore(LocalDateTime.now())) {
            return false; // Expired link
        }

        return true; // Valid request
    }

    @Override
    public boolean resetPassword(String hashId, PasswordResetDTO passwordResetDTO) {
        Optional<PasswordChangeRequest> requestOptional = passwordChangeRequestRepository.findByHashId(hashId);
        if (requestOptional.isEmpty()) {
            return false; // Invalid hashId
        }

        PasswordChangeRequest passwordChangeRequest = requestOptional.get();
        if (passwordChangeRequest.getExpiry().isBefore(LocalDateTime.now())) {
            return false; // Expired request
        }

        // If you want, you can add further validation here too.
        if (!passwordResetDTO.getNewPassword().equals(passwordResetDTO.getConfirmNewPassword())) {
            return false; // Passwords do not match
        }

        Users user = passwordChangeRequest.getUser();
        user.setPassword(passwordResetDTO.getNewPassword());

        userRepository.save(user);
        passwordChangeRequestRepository.delete(passwordChangeRequest); // Delete the request after processing

        return true;
    }

}
