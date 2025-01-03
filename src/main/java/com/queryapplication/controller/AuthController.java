package com.queryapplication.controller;

import com.queryapplication.dto.UserDTO;
import com.queryapplication.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.queryapplication.dto.LoginDTO;

@RestController
@RequestMapping("/api/v1/queryapplication/auth")
public class AuthController {

    @Autowired
    private UsersService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        UserDTO userDTO = userService.login(loginDTO.getEmail(), loginDTO.getPassword());
        if (userDTO != null) {
            return ResponseEntity.ok(userDTO);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
    }
}
