package com.queryapplication.controller;

import com.queryapplication.dto.CreateAdminDTO;
import com.queryapplication.dto.UpdateAdminDTO;
import com.queryapplication.entity.Users;
import com.queryapplication.service.AdminService;
import com.queryapplication.service.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/v1/queryapplication/admin")
public class AdminController {
    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
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
    public ResponseEntity<Users> toggleAdminStatus(@PathVariable Long adminId) {
        Users updatedAdmin = adminService.toggleAdminStatus(adminId);
        return new ResponseEntity<>(updatedAdmin, HttpStatus.OK);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<Users> getUserDetails(@PathVariable Long userId) {
        Users user = adminService.getUserDetails(userId);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PatchMapping("/users/{userId}")
    public ResponseEntity<Users> editUser(@PathVariable Long userId, @RequestBody UpdateAdminDTO updateAdminDTO) {
        Users updatedUser = adminService.editUser(userId, updateAdminDTO);
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
