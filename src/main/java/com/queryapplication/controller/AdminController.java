package com.queryapplication.controller;

import com.queryapplication.constants.ActivityConstants;
import com.queryapplication.dto.CreateAdminDTO;
import com.queryapplication.dto.UserDTO;
import com.queryapplication.entity.Status;
import com.queryapplication.dto.UpdateAdminDTO;
import com.queryapplication.entity.Users;
import com.queryapplication.repository.UserRepository;
import com.queryapplication.service.AdminService;
import com.queryapplication.service.ActivityLogService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final ActivityLogService activityLogService;
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
    public ResponseEntity<?> createAdmin(
            @Valid @RequestBody CreateAdminDTO createAdminDTO,
            Authentication authentication
    ) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Authentication required");
        }

        try {
            if (userRepository.existsByUsername(createAdminDTO.getUsername())) {
                return ResponseEntity.badRequest()
                        .body("Username is already taken!");
            }

            if (userRepository.existsByEmail(createAdminDTO.getEmail())) {
                return ResponseEntity.badRequest()
                        .body("Email is already in use!");
            }

            Users newAdmin = adminService.createAdmin(createAdminDTO);

            activityLogService.logActivity(
                    "Created new admin",
                    String.format(" with username: %s",
                            newAdmin.getUsername())
            );

            return new ResponseEntity<>(newAdmin, HttpStatus.CREATED);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error creating admin: " + e.getMessage());
        }
    }

    @PutMapping("/toggle-status/{adminId}")
    public ResponseEntity toggleAdminStatus(@PathVariable Long adminId, @RequestBody Map<String, Long> requestData, Authentication authentication) {
        Long userId = requestData.get("userId");

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Users updatedAdmin = adminService.toggleAdminStatus(adminId);

        activityLogService.logActivity("Toggled admin status",
                String.format(" for admin: %s",
                        user.getUsername()));
        return new ResponseEntity<>(updatedAdmin, HttpStatus.OK);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<Users> getUserDetails(@PathVariable Long userId) {
        Users user = adminService.getUserDetails(userId);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }


    @PatchMapping("/edit")
    public ResponseEntity<Users> editUser(@RequestBody Map<String, Object> requestData, Authentication authentication) {
        Long userId = Long.parseLong(requestData.get("userId").toString());
        UpdateAdminDTO updateAdminDTO = new UpdateAdminDTO();
        if (requestData.containsKey("firstName")) {
            updateAdminDTO.setFirstName((String) requestData.get("firstName"));
        }
        if (requestData.containsKey("email")) {
            updateAdminDTO.setEmail((String) requestData.get("email"));
        }
        if (requestData.containsKey("username")) {
            updateAdminDTO.setUsername((String) requestData.get("username"));
        }
        if (requestData.containsKey("location")) {
            updateAdminDTO.setLocation((String) requestData.get("location"));
        }

        Users updatedUser = adminService.editUser(userId, updateAdminDTO);

        activityLogService.logActivity("Edited user details",
                String.format(" for admin: %s",
                        updatedUser.getUsername()));

        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }
    @GetMapping("/users-names")
    public ResponseEntity<List<UserDTO>> getAllUserNames() {
        try {
            Iterable<Users> users   = adminService.getAllUsers();
            List<UserDTO> userDTOs = StreamSupport.stream(users.spliterator(), false)
                    .map(user -> {
                        UserDTO dto = new UserDTO();
                        dto.setId(user.getId());
                        dto.setUsername(user.getUsername());
                        return dto;
                    })
                    .collect(Collectors.toList());
            return new ResponseEntity<>(userDTOs, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }
}
