package com.queryapplication.dto;

import com.queryapplication.entity.Status;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class UserDTO {
    private String firstName;
    private String username;
    private String email;
    private String location;
    private Status status;
    private Set<RoleDTO> roles;
}
