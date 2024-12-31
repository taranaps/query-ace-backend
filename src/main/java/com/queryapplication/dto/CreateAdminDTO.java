package com.queryapplication.dto;

import com.queryapplication.entity.LocationName;
import com.queryapplication.entity.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateAdminDTO {

    @NotNull
    private String firstName;

    @NotNull
    @Email
    private String email;

    @NotNull
    private LocationName location;

    @NotNull
    @Size(min = 5)
    private String username;

    @NotNull
    @Size(min = 8)
    private String password;

    @NotNull
    private RoleName userRole;
}
