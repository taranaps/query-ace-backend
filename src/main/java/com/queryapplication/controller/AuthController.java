package com.queryapplication.controller;

import com.queryapplication.dto.SignUpRequest;
import com.queryapplication.entity.Role;
import com.queryapplication.entity.Status;
import com.queryapplication.entity.Users;
import com.queryapplication.repository.RoleRepository;
import com.queryapplication.repository.UserRepository;
import com.queryapplication.response.JwtResponse;
import com.queryapplication.security.JwtTokenProvider;
import com.queryapplication.security.UserPrincipal;
import com.queryapplication.service.UsersService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import com.queryapplication.dto.LoginDTO;

import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/queryapplication/auth")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")

public class AuthController {

    @Autowired
    private final UsersService usersService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body("Error: Email is already in use!");
        }

        Users user = new Users();
        user.setUsername(signUpRequest.getUsername());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setEmail(signUpRequest.getEmail());
        user.setFirstName(signUpRequest.getFirstName());
        user.setLocation(signUpRequest.getLocation());
        user.setStatus(Status.ACTIVE);

        Role role = roleRepository.findByRoleName(signUpRequest.getRole())
                .orElseThrow(() -> new RuntimeException("Error: Role not found."));
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);

        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        try {
            logger.info("Login attempt for email: {}", loginDTO.getEmail());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDTO.getEmail(),
                            loginDTO.getPassword()
                    )
            );

            logger.debug("Authentication successful for user: {}", authentication.getName());

            String jwt = jwtTokenProvider.generateToken(authentication);

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            String role = userPrincipal.getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .orElse("");

            logger.info("User logged in successfully: {}, Role: {}", loginDTO.getEmail(), role);

            JwtResponse response = new JwtResponse(jwt, "Bearer", role);
            logger.debug("Generated token: {}", jwt);

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            logger.error("Login failed - Bad credentials for email: {}", loginDTO.getEmail(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid email or password");
        } catch (Exception e) {
            logger.error("Login failed - Unexpected error for email: {}", loginDTO.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred");
        }
    }

}