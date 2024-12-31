package com.queryapplication.service;

import com.queryapplication.dto.CreateAdminDTO;
import com.queryapplication.entity.Users;

public interface AdminService {
    Users createAdmin(CreateAdminDTO createAdminDTO);

    Iterable<Users> getAllUsers();

    Users toggleAdminStatus(Long adminId);

    Users getUserDetails(Long userId);

    Users editUser(Long userId, String firstName, String email, String location, String username);
}
