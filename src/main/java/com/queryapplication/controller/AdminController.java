package com.queryapplication.controller;

import com.queryapplication.constants.ActivityConstants;
import com.queryapplication.dto.CreateAdminDTO;
import com.queryapplication.entity.Status;
import com.queryapplication.entity.Users;
import com.queryapplication.service.AdminService;
import com.queryapplication.service.ActivityLogService; // Import the ActivityLogService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;  // Add this import
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/queryapplication/admin")
public class AdminController {

    private final AdminService adminService;
    private final ActivityLogService activityLogService; // Declare ActivityLogService

    // Inject ActivityLogService along with AdminService
    @Autowired
    public AdminController(AdminService adminService, ActivityLogService activityLogService) {
        this.adminService = adminService;
        this.activityLogService = activityLogService;
    }

    // Get the currently authenticated user from the Authentication object
    private Users getAuthenticatedUser(Authentication authentication) {
        // Assuming the username is the user's unique identifier, you can adjust this to suit your auth model
        String username = authentication.getName();
        return adminService.getUserByUsername(username); // Ensure adminService has a method to fetch user by username
    }

    @GetMapping("/users")
    public ResponseEntity<Iterable<Users>> getAllUsers(Authentication authentication) {
        Users user = getAuthenticatedUser(authentication); // Get the authenticated user
        Iterable<Users> users = adminService.getAllUsers();

        // Log activity
        activityLogService.logActivity(user, "Fetched all users", "Admin viewed the list of all users.");

        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<Users> createAdmin(@RequestBody CreateAdminDTO createAdminDTO, Authentication authentication) {
        Users user = getAuthenticatedUser(authentication); // Get the authenticated user
        Users newAdmin = adminService.createAdmin(createAdminDTO);

        // Log activity
        activityLogService.logActivity(user, "Created new admin", "Admin created a new admin with username: " + newAdmin.getUsername());

        return new ResponseEntity<>(newAdmin, HttpStatus.CREATED);
    }

    @PutMapping("/toggle-status/{adminId}")
    public ResponseEntity<Users> toggleAdminStatus(@PathVariable Long adminId, Authentication authentication) {
        Users user = getAuthenticatedUser(authentication); // Get the authenticated user
        Users updatedAdmin = adminService.toggleAdminStatus(adminId);

        // Log activity
        activityLogService.logActivity(user, "Toggled admin status", "Admin toggled the status for admin ID: " + adminId);

        return new ResponseEntity<>(updatedAdmin, HttpStatus.OK);
    }

    @GetMapping("/details/{userId}")
    public ResponseEntity<Users> getUserDetails(@PathVariable Long userId, Authentication authentication) {
        Users user = getAuthenticatedUser(authentication); // Get the authenticated user
        Users userDetails = adminService.getUserDetails(userId);

        // Log activity
        activityLogService.logActivity(user, "Fetched user details", "Admin viewed the details for user ID: " + userId);

        return new ResponseEntity<>(userDetails, HttpStatus.OK);
    }

    @PatchMapping("/edit/{userId}")
    public ResponseEntity<Users> editUser(@PathVariable Long userId, @RequestParam(required = false) String firstName, @RequestParam(required = false) String email, @RequestParam(required = false) String location, @RequestParam(required = false) String username, Authentication authentication) {
        Users user = getAuthenticatedUser(authentication); // Get the authenticated user
        Users updatedUser = adminService.editUser(userId, firstName, email, location, username);

        // Log activity
        activityLogService.logActivity(user, "Edited user details", "Admin edited user ID: " + userId + " with new details.");

        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }
    @PutMapping("/toggle-status-with-log/{adminId}")
    public ResponseEntity<Users> logAdminStatusToggle(@PathVariable Long adminId, Authentication authentication) {
        Users performer = getAuthenticatedUser(authentication);
        Users updatedAdmin = adminService.toggleAdminStatus(adminId);

        // Log the activity
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
