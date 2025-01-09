package com.queryapplication.controller;

import com.queryapplication.dto.CreateAdminDTO;
import com.queryapplication.entity.LocationName;
import com.queryapplication.entity.RoleName;
import com.queryapplication.entity.Status;
import com.queryapplication.entity.Users;
import com.queryapplication.service.AdminService;
import com.queryapplication.service.QueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(AdminController.class)
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private AdminService adminService;

    private Users mockUser;

    @BeforeEach
    public void setUp() {
        mockUser = new Users();
        mockUser.setId(1L);
        mockUser.setFirstName("Test User");
        mockUser.setEmail("test@example.com");
        mockUser.setUsername("testuser");
        mockUser.setStatus(Status.ACTIVE);
    }

    @Test
    @DisplayName("Test getAllUsers - Success")
    public void givenValidUsers_whenGetAllUsers_thenReturnOk() throws Exception {
        when(adminService.getAllUsers()).thenReturn(Collections.singletonList(mockUser));
        mockMvc.perform(get("/api/v1/queryapplication/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Test User"))
                .andExpect(jsonPath("$[0].email").value("test@example.com"));
    }

    @Test
    @DisplayName("Test createAdmin - Success")
    public void givenValidCreateAdminRequest_whenCreateAdmin_thenReturnCreated() throws Exception {
        CreateAdminDTO createAdminDTO = new CreateAdminDTO();
        createAdminDTO.setFirstName("Test Admin");
        createAdminDTO.setEmail("admin@example.com");
        createAdminDTO.setUsername("admin");
        createAdminDTO.setPassword("password123");
        createAdminDTO.setLocation(LocationName.TRIVANDRUM);
        createAdminDTO.setUserRole(RoleName.ADMIN);

        when(adminService.createAdmin(any(CreateAdminDTO.class))).thenReturn(mockUser);

        mockMvc.perform(post("/api/v1/queryapplication/admin/create")
                        .contentType("application/json")
                        .content("{ \"firstName\": \"Test Admin\", \"email\": \"admin@example.com\", \"username\": \"admin\", \"password\": \"password123\", \"location\": \"Location\", \"userRole\": \"ADMIN\" }"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Test User"));
    }

    @Test
    @DisplayName("Test toggleAdminStatus - Active to Inactive")
    public void givenValidAdminId_whenToggleAdminStatus_thenReturnOk() throws Exception {
        when(adminService.toggleAdminStatus(1L)).thenReturn(mockUser);

        mockMvc.perform(put("/api/v1/queryapplication/admin/toggle-status/{adminId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Test getUserDetails - Success")
    public void givenValidUserId_whenGetUserDetails_thenReturnOk() throws Exception {
        when(adminService.getUserDetails(1L)).thenReturn(mockUser);

        mockMvc.perform(get("/api/v1/queryapplication/admin/details/{userId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("Test getAllUserNames - Success")
    public void givenUsers_whenGetAllUserNames_thenReturnOk() throws Exception {
        when(adminService.getAllUsers()).thenReturn(Collections.singletonList(mockUser));
        mockMvc.perform(get("/api/v1/queryapplication/admin/users-names"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("testuser"));
    }

    @Test
    @DisplayName("Test getAllUserNames - Error")
    public void givenException_whenGetAllUserNames_thenReturnInternalServerError() throws Exception {
        when(adminService.getAllUsers()).thenThrow(new RuntimeException("Something went wrong"));

        mockMvc.perform(get("/api/v1/queryapplication/admin/users-names"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$").isEmpty());
    }
}
