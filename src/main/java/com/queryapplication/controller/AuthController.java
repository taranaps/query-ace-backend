package com.queryapplication.controller;

import com.queryapplication.dto.PasswordResetDTO;
import com.queryapplication.dto.UserDTO;
import com.queryapplication.service.PasswordService;
import com.queryapplication.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.queryapplication.dto.LoginDTO;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/queryapplication/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {
    private UsersService userService;
    private PasswordService passwordService;

    @Autowired
    public AuthController(UsersService userService, PasswordService passwordService) {
        this.userService = userService;
        this.passwordService = passwordService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        UserDTO userDTO = userService.login(loginDTO.getEmail(), loginDTO.getPassword());
        if (userDTO != null) {
            return ResponseEntity.ok(userDTO);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
    }

    @PostMapping("/request-password-reset")
    public ResponseEntity<?> requestPasswordReset(@RequestParam String email) {
        boolean result = passwordService.initiatePasswordReset(email);
        if (result) {
            return ResponseEntity.ok("Password reset link has been sent to your email.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email not found.");
    }

    @GetMapping("/reset-password/{hashId}")
    public ResponseEntity<?> resetPassword(@PathVariable String hashId) {
        boolean isValid = passwordService.validatePasswordReset(hashId);
        if (isValid) {
            String frontendResetPasswordUrl = "http://localhost:3000/pages/reset-password?token=" + hashId;
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(frontendResetPasswordUrl));
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired password reset link.");
    }

    @PostMapping("/reset-password/{hashId}")
    public ResponseEntity<?> resetPassword(@PathVariable String hashId, @RequestBody PasswordResetDTO passwordResetDTO) {
        if (!passwordResetDTO.getNewPassword().equals(passwordResetDTO.getConfirmNewPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Passwords do not match.");
        }

        boolean result = passwordService.resetPassword(hashId, passwordResetDTO);
        if (result) {
            return ResponseEntity.ok("Password has been successfully reset.");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to reset password.");
    }
}
