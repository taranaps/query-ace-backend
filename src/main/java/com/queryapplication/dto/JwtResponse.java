package com.queryapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private String role;

    public JwtResponse(String token, String type, String role) {
        this.token = token;
        this.type = type;
        this.role = role;
    }
}

