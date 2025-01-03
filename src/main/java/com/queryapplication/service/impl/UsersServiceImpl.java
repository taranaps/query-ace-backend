package com.queryapplication.service.impl;

import com.queryapplication.dto.UserDTO;
import com.queryapplication.entity.*;
import com.queryapplication.repository.RoleRepository;
import com.queryapplication.repository.UserRepository;
import com.queryapplication.service.UsersService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class UsersServiceImpl implements UsersService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public UsersServiceImpl(UserRepository userRepository, RoleRepository roleRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.modelMapper = modelMapper;
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

    public UserDTO login(String email, String password) {
        Users user = userRepository.findByEmailAndPassword(email, password);

        if (user != null) {
            return modelMapper.map(user, UserDTO.class);
        }

        return null;
    }
}
