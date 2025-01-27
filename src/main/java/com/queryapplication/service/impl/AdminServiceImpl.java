package com.queryapplication.service.impl;

import com.queryapplication.dto.CreateAdminDTO;
import com.queryapplication.dto.UpdateAdminDTO;
import com.queryapplication.entity.*;
import com.queryapplication.exception.ResourceNotFoundException;
import com.queryapplication.repository.RoleRepository;
import com.queryapplication.repository.UserRepository;
import com.queryapplication.service.AdminService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;


    @Autowired
    public AdminServiceImpl(UserRepository userRepository, RoleRepository roleRepository,PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Users createAdmin(CreateAdminDTO createAdminDTO) {
        if (userRepository.existsByEmail(createAdminDTO.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        if (userRepository.existsByUsername(createAdminDTO.getUsername())) {
            throw new RuntimeException("Username already in use");
        }

        Users newUser = new Users();
        newUser.setFirstName(createAdminDTO.getFirstName());
        newUser.setEmail(createAdminDTO.getEmail());
        newUser.setUsername(createAdminDTO.getUsername());
        newUser.setPassword(passwordEncoder.encode(createAdminDTO.getPassword()));
        newUser.setLocation(LocationName.valueOf(String.valueOf(createAdminDTO.getLocation())));
        newUser.setStatus(Status.ACTIVE);

        Role role = roleRepository.findByRoleName(RoleName.valueOf(String.valueOf(createAdminDTO.getUserRole())))
                .orElseThrow(() -> new RuntimeException("Role not found"));

        Set<Role> roles = new HashSet<>();
        roles.add(role);
        newUser.setRoles(roles);

        return userRepository.save(newUser);
    }

    @Override
    public Iterable<Users> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Users getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public Iterable<Users> getAdminUsers() {
        // Fetch users who have the ADMIN role
        return userRepository.findByRoleName(RoleName.ADMIN);
    }

    @Override
    public Users toggleAdminStatus(Long adminId) {
        Users admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (admin.getStatus() == Status.ACTIVE) {
            admin.setStatus(Status.INACTIVE);
        } else {
            admin.setStatus(Status.ACTIVE);
        }

        return userRepository.save(admin);
    }

    @Override
    public Users getUserDetails(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user;
    }

    @Override
    @Transactional
    public Users editUser(Long userId, UpdateAdminDTO updateAdminDTO) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));

        if (updateAdminDTO.getFirstName() != null && !updateAdminDTO.getFirstName().isEmpty()) {
            user.setFirstName(updateAdminDTO.getFirstName());
        }
        if (updateAdminDTO.getEmail() != null && !updateAdminDTO.getEmail().trim().isEmpty()) {
            String newEmail = updateAdminDTO.getEmail().trim();
            if (!newEmail.equals(user.getEmail())) {
                Users existingUserWithEmail = userRepository.findByEmail(newEmail).orElse(null);
                if (existingUserWithEmail != null && !existingUserWithEmail.getId().equals(userId)) {
                    throw new RuntimeException("Email already in use!");
                }
                user.setEmail(newEmail);
            }
        }

        if (updateAdminDTO.getUsername() != null && !updateAdminDTO.getUsername().isEmpty()) {
            if (!updateAdminDTO.getUsername().equals(user.getUsername())) {
                if (userRepository.existsByUsername(updateAdminDTO.getUsername())) {
                    throw new RuntimeException("Username already taken!");
                }
                user.setUsername(updateAdminDTO.getUsername());
            }
        }
        if (updateAdminDTO.getLocation() != null && !updateAdminDTO.getLocation().isEmpty()) {
            try {
                LocationName locationName = LocationName.valueOf(updateAdminDTO.getLocation().toUpperCase());
                user.setLocation(locationName);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid location: " + updateAdminDTO.getLocation());
            }
        }

        return userRepository.save(user);
    }
}
