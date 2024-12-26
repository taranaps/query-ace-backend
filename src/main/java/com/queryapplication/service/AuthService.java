package com.queryapplication.service;

import com.queryapplication.dto.AddAdminDTO;
import com.queryapplication.dto.AdminStatusUpdateDTO;
import com.queryapplication.dto.LoginDTO;
import com.queryapplication.dto.RegisterDTO;

public interface AuthService {
    // Existing methods
    RegisterDTO register(RegisterDTO registerDTO);
    String login(LoginDTO loginDTO);

    // New methods
    RegisterDTO addAdmin(AddAdminDTO addAdminDTO);
    String updateAdminStatus(AdminStatusUpdateDTO adminStatusUpdateDTO);
}
