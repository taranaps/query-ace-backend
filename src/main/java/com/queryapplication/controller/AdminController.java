package com.queryapplication.controller;

import com.queryapplication.dto.CreateAdminDTO;
import com.queryapplication.entity.Users;
import com.queryapplication.service.AdminService;
import com.queryapplication.service.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

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

    @GetMapping("/details/{userId}")
    public ResponseEntity<Users> getUserDetails(@PathVariable Long userId) {
        Users user = adminService.getUserDetails(userId);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PatchMapping("/edit/{userId}")
    public ResponseEntity<Users> editUser(@PathVariable Long userId, @RequestParam(required = false) String firstName, @RequestParam(required = false) String email, @RequestParam(required = false) String location, @RequestParam(required = false) String username) {
        Users updatedUser = adminService.editUser(userId, firstName, email, location, username);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }
}