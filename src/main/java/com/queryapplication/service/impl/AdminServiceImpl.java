package com.queryapplication.service.impl;

import com.queryapplication.dto.CreateAdminDTO;
import com.queryapplication.dto.UpdateAdminDTO;
import com.queryapplication.entity.*;
import com.queryapplication.exception.ResourceNotFoundException;
import com.queryapplication.repository.RoleRepository;
import com.queryapplication.repository.UserRepository;
import com.queryapplication.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public AdminServiceImpl(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
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
        newUser.setPassword(createAdminDTO.getPassword());
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
    public Users editUser(Long userId, UpdateAdminDTO updateAdminDTO) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));

        if (updateAdminDTO.getFirstName() != null && !updateAdminDTO.getFirstName().isEmpty()) {
            user.setFirstName(updateAdminDTO.getFirstName());
        }
        if (updateAdminDTO.getEmail() != null && !updateAdminDTO.getEmail().isEmpty()) {
            user.setEmail(updateAdminDTO.getEmail());
        }
        if (updateAdminDTO.getUsername() != null && !updateAdminDTO.getUsername().isEmpty()) {
            user.setUsername(updateAdminDTO.getUsername());
        }
        if (updateAdminDTO.getLocation() != null && !updateAdminDTO.getLocation().isEmpty()) {
            try {
                LocationName locationName = LocationName.valueOf(updateAdminDTO.getLocation().toUpperCase());
                user.setLocation(locationName);
            } catch (IllegalArgumentException e) {
                throw new ResourceNotFoundException("Invalid location: " + updateAdminDTO.getLocation());
            }
        }

        return userRepository.save(user);
    }
}
