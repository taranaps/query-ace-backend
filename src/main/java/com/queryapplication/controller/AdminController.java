package com.queryapplication.controller;

import com.queryapplication.constants.ActivityConstants;
import com.queryapplication.dto.CreateAdminDTO;
import com.queryapplication.entity.Status;
import com.queryapplication.dto.UpdateAdminDTO;
import com.queryapplication.entity.Users;
import com.queryapplication.repository.UserRepository;
import com.queryapplication.service.AdminService;
import com.queryapplication.service.ActivityLogService; // Import the ActivityLogService
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@SecurityRequirement(name = "Bearer Authentication")
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

    @GetMapping("/admins")
    public ResponseEntity<Iterable<Users>> getAdminUsers() {
        Iterable<Users> adminUsers = adminService.getAdminUsers();
        return new ResponseEntity<>(adminUsers, HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<Users> createAdmin(@RequestBody CreateAdminDTO createAdminDTO) {
        Users newAdmin = adminService.createAdmin(createAdminDTO);
        return new ResponseEntity<>(newAdmin, HttpStatus.CREATED);
    }

    @PutMapping("/toggle-status/{adminId}")
    public ResponseEntity<Users> toggleAdminStatus(@PathVariable Long adminId)
    {
        Users updatedAdmin = adminService.toggleAdminStatus(adminId);
        return new ResponseEntity<>(updatedAdmin, HttpStatus.OK);
    }


    @PatchMapping("/edit")
    public ResponseEntity<Users> editUser(@RequestBody Map<String, Object> requestData) {

        if (requestData.get("userId") == null) {
            throw new IllegalArgumentException("userId is required");
        }
        Long userId = Long.parseLong(requestData.get("userId").toString());
        String firstName = (String) requestData.get("firstName");
        String email = (String) requestData.get("email");
        String location = (String) requestData.get("location");
        String username = (String) requestData.get("username");

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Users updatedUser = adminService.editUser(userId, firstName, email, location, username);

        activityLogService.logActivity(

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


}