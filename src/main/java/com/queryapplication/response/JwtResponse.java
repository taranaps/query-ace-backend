package com.queryapplication.response;

import lombok.Data;

@Data
public class JwtResponse {
    private String token;
    private String type;
    private String role;
    private Long userId;
    private String username;
    private String email;


    public JwtResponse(String token, String type, String role,Long userId, String username, String email) {
        this.token = token;
        this.type = type;
        this.role = role;
        this.userId = userId;
        this.username = username;
        this.email = email;
    }
}