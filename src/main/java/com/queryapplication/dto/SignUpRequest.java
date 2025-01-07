package com.queryapplication.dto;

import com.queryapplication.entity.LocationName;
import com.queryapplication.entity.Role;
import com.queryapplication.entity.RoleName;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class SignUpRequest {
    private String firstName;
    private String username;
    @Email
    private String email;
    private String password;
    private LocationName location;
    private RoleName role;
}