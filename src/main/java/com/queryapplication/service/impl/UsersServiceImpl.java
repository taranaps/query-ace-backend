package com.queryapplication.service.impl;

import com.queryapplication.entity.*;
import com.queryapplication.repository.RoleRepository;
import com.queryapplication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.Set;

public class UsersServiceImpl {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public UsersServiceImpl(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public Users registerUser(String username, String firstName, String email, String password, LocationName location) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already in use");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already in use");
        }

        Users user = new Users();
        user.setFirstName(firstName);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setLocation(location);
        user.setStatus(Status.ACTIVE);

        Role adminRole = roleRepository.findByRoleName(RoleName.ADMIN)
                .orElseThrow(() -> new RuntimeException("ADMIN role not found!"));
        user.setRoles(Set.of(adminRole));

        return userRepository.save(user);
    }

    public Optional<Users> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
