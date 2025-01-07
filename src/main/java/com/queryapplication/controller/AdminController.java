package com.queryapplication.controller;

import com.queryapplication.constants.ActivityConstants;
import com.queryapplication.dto.CreateAdminDTO;
import com.queryapplication.entity.Status;
import com.queryapplication.entity.Users;
import com.queryapplication.repository.UserRepository;
import com.queryapplication.service.AdminService;
import com.queryapplication.service.ActivityLogService; // Import the ActivityLogService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;  // Add this import
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Map;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/v1/queryapplication/admin")
public class AdminController {
    private final AdminService adminService;
    private final ActivityLogService activityLogService; // Declare ActivityLogService
    private final UserRepository userRepository;

    @Autowired
    public AdminController(AdminService adminService, ActivityLogService activityLogService,UserRepository userRepository) {
        this.adminService = adminService;
        this.activityLogService = activityLogService;
        this.userRepository = userRepository;
    }

    private Users getAuthenticatedUser(Authentication authentication) {
        String username = authentication.getName();
        return adminService.getUserByUsername(username);
    }

    @GetMapping("/users")
    public ResponseEntity<Iterable<Users>> getAllUsers() {
        Iterable<Users> users = adminService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<Users> createAdmin(@RequestBody CreateAdminDTO createAdminDTO) {
        Users newAdmin = adminService.createAdmin(createAdminDTO);

        // Log activity
        activityLogService.logActivity(newAdmin, "Created new admin", "Admin created a new admin with username: " + newAdmin.getUsername());

        return new ResponseEntity<>(newAdmin, HttpStatus.CREATED);
    }

    @PutMapping("/toggle-status/{adminId}")
    public ResponseEntity toggleAdminStatus(@PathVariable Long adminId, @RequestBody Map<String, Long> requestData) {
        Long userId = requestData.get("userId"); // Fetch userId from the request body

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Users updatedAdmin = adminService.toggleAdminStatus(adminId);

        activityLogService.logActivity(user, "Toggled admin status", "Admin toggled the status for admin ID: " + adminId);

        return new ResponseEntity<>(updatedAdmin, HttpStatus.OK);
    }


    @GetMapping("/details")
    public ResponseEntity<Users> getUserDetails(@RequestParam Long userId) {
        Users userDetails = adminService.getUserDetails(userId);

        Users adminUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        activityLogService.logActivity(adminUser, "Fetched user details", "Admin viewed the details for user ID: " + userId);

        return new ResponseEntity<>(userDetails, HttpStatus.OK);
    }


    @PatchMapping("/edit")
    public ResponseEntity<Users> editUser(@RequestBody Map<String, Object> requestData) {
        Long userId = Long.parseLong(requestData.get("userId").toString());
        String firstName = (String) requestData.get("firstName");
        String email = (String) requestData.get("email");
        String location = (String) requestData.get("location");
        String username = (String) requestData.get("username");

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Users updatedUser = adminService.editUser(userId, firstName, email, location, username);

        activityLogService.logActivity(
                user,
                "Edited user details",
                String.format("Admin edited user ID: %d with new details.", userId)
        );

        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }
    @GetMapping("/users-names")
    public ResponseEntity<List<String>> getAllUserNames() {
        try {
            Iterable<Users> users = adminService.getAllUsers();  // Fetch all users
            List<String> userNames = StreamSupport.stream(users.spliterator(), false)
                    .map(Users::getUsername) // Assuming User has a getUsername() method
                    .collect(Collectors.toList());
            return new ResponseEntity<>(userNames, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    @PutMapping("/toggle-status-with-log")
    public ResponseEntity<Users> logAdminStatusToggle(@RequestBody Map<String, Long> requestData) {
        Long userId = requestData.get("userId");
        Long adminId = requestData.get("adminId");

        Users performer = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Users updatedAdmin = adminService.toggleAdminStatus(adminId);

        String action = updatedAdmin.getStatus() == Status.ACTIVE ?
                ActivityConstants.USER_ENABLED : ActivityConstants.USER_DISABLED;
        activityLogService.logActivity(
                performer,
                action,
                String.format("%s (%s)", updatedAdmin.getFirstName(), updatedAdmin.getEmail())
        );

        return new ResponseEntity<>(updatedAdmin, HttpStatus.OK);
    }


}