package com.queryapplication.service;

import com.queryapplication.dto.UserDTO;
import com.queryapplication.entity.LocationName;
import com.queryapplication.entity.Users;

import java.util.Optional;

public interface UsersService {

    public Users registerUser(String username, String firstName, String email, String password, LocationName location);

    public Optional<Users> findByUsername(String username);

    UserDTO login(String email, String password);


}
