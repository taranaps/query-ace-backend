package com.queryapplication.controller;

import com.queryapplication.dto.AddAdminDTO;
import com.queryapplication.dto.AdminStatusUpdateDTO;
import com.queryapplication.dto.LoginDTO;
import com.queryapplication.dto.RegisterDTO;
import com.queryapplication.response.ResponseHandler;
import com.queryapplication.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/queryapplication")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Register endpoint
    @PostMapping("/register")
    public ResponseEntity<Object> register(@Valid @RequestBody RegisterDTO registerDTO) {
        RegisterDTO registeredUser = authService.register(registerDTO);
        return new ResponseHandler().responseBuilder("Registered Successfully!", HttpStatus.OK, registeredUser);
    }

    // Login endpoint
    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody LoginDTO loginDTO) {
        String token = authService.login(loginDTO);
        return new ResponseHandler().responseBuilder("Logged in Successfully!", HttpStatus.OK, token);
    }

    // Add Admin endpoint (accessible only by Super Admin)
    @PostMapping("/super-admin/add-admin")
    public ResponseEntity<Object> addAdmin(@Valid @RequestBody AddAdminDTO addAdminDTO) {
        RegisterDTO addedAdmin = authService.addAdmin(addAdminDTO);
        return new ResponseHandler().responseBuilder("Admin Added Successfully!", HttpStatus.OK, addedAdmin);
    }

    // Update Admin Status endpoint (accessible only by Super Admin)
    @PutMapping("/super-admin/update-admin-status")
    public ResponseEntity<Object> updateAdminStatus(@Valid @RequestBody AdminStatusUpdateDTO adminStatusUpdateDTO) {
        String message = authService.updateAdminStatus(adminStatusUpdateDTO);
        return new ResponseHandler().responseBuilder(message, HttpStatus.OK, null);
    }
}
