package com.queryapplication.service.impl;

import com.queryapplication.dto.CreateAdminDTO;
import com.queryapplication.entity.*;
import com.queryapplication.repository.RoleRepository;
import com.queryapplication.repository.UserRepository;
import com.queryapplication.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
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
    public Users editUser(Long userId, String firstName, String email, String location, String username) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (firstName != null && !firstName.isEmpty()) {
            user.setFirstName(firstName);
        }
        if (email != null && !email.isEmpty()) {
            user.setEmail(email);
        }
        if (location != null && !location.isEmpty()) {
            user.setLocation(LocationName.valueOf(location));
        }
        if (username != null && !username.isEmpty()) {
            user.setUsername(username);
        }

        return userRepository.save(user);
    }
}
