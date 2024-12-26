package com.queryapplication.service.impl;

import com.queryapplication.dto.AddAdminDTO;
import com.queryapplication.dto.AdminStatusUpdateDTO;
import com.queryapplication.dto.LoginDTO;
import com.queryapplication.dto.RegisterDTO;
import com.queryapplication.entity.Role;
import com.queryapplication.entity.RoleName;
import com.queryapplication.entity.Status;
import com.queryapplication.entity.Users;
import com.queryapplication.exception.APIBadRequestException;
import com.queryapplication.repository.RoleRepository;
import com.queryapplication.repository.UserRepository;
import com.queryapplication.security.JwtTokenProvider;
import com.queryapplication.service.AuthService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AuthServiceImpl(UserRepository userRepository, RoleRepository roleRepository, ModelMapper modelMapper,
                           PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                           JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional
    public RegisterDTO register(RegisterDTO registerDTO) {
        // Check if username exists
        if (userRepository.existsByUsername(registerDTO.getUsername())) {
            throw new APIBadRequestException(HttpStatus.BAD_REQUEST, "Username already exists!");
        }

        // Check if email exists
        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            throw new APIBadRequestException(HttpStatus.BAD_REQUEST, "Email already exists!");
        }

        // Map DTO to entity
        Users user = modelMapper.map(registerDTO, Users.class);
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setStatus(Status.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // Assign default role (could be dynamic based on input)
        Role userRole = roleRepository.findByRoleName(RoleName.ADMIN) // Allow dynamic role assignment if needed
                .orElseThrow(() -> new APIBadRequestException(HttpStatus.NOT_FOUND, "User Role not set."));
        user.getRoles().add(userRole);

        // Save user
        Users newUser = userRepository.save(user);

        // Map back to DTO
        RegisterDTO newRegisterDTO = modelMapper.map(newUser, RegisterDTO.class);
        return newRegisterDTO;
    }

    @Override
    @Transactional
    public String login(LoginDTO loginDTO) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDTO.getEmail(),
                        loginDTO.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(authentication);

        return token;
    }

    @Override
    @Transactional
    public RegisterDTO addAdmin(AddAdminDTO addAdminDTO) {
        // Check if username exists
        if (userRepository.existsByUsername(addAdminDTO.getUsername())) {
            throw new APIBadRequestException(HttpStatus.BAD_REQUEST, "Username already exists!");
        }

        // Check if email exists
        if (userRepository.existsByEmail(addAdminDTO.getEmail())) {
            throw new APIBadRequestException(HttpStatus.BAD_REQUEST, "Email already exists!");
        }

        // Map DTO to entity
        Users admin = modelMapper.map(addAdminDTO, Users.class);
        admin.setPassword(passwordEncoder.encode(addAdminDTO.getPassword()));
        admin.setStatus(Status.ACTIVE);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());

        // Assign ADMIN role
        Role adminRole = roleRepository.findByRoleName(RoleName.ADMIN)
                .orElseThrow(() -> new APIBadRequestException(HttpStatus.NOT_FOUND, "Admin role not found!"));
        admin.getRoles().add(adminRole);

        // Save admin
        Users newAdmin = userRepository.save(admin);

        // Map back to DTO
        RegisterDTO newRegisterDTO = modelMapper.map(newAdmin, RegisterDTO.class);
        return newRegisterDTO;
    }

    @Override
    @Transactional
    public String updateAdminStatus(AdminStatusUpdateDTO adminStatusUpdateDTO) {
        Users admin = userRepository.findById(adminStatusUpdateDTO.getId())
                .orElseThrow(() -> new APIBadRequestException(HttpStatus.NOT_FOUND, "Admin not found!"));

        // Ensure the user has ADMIN role
        boolean isAdmin = admin.getRoles().stream()
                .anyMatch(role -> role.getRoleName() == RoleName.ADMIN);
        if (!isAdmin) {
            throw new APIBadRequestException(HttpStatus.BAD_REQUEST, "User is not an Admin!");
        }

        admin.setStatus(adminStatusUpdateDTO.getStatus());
        admin.setUpdatedAt(LocalDateTime.now());
        userRepository.save(admin);

        return "Admin status updated successfully!";
    }
}
