package com.queryapplication.service;

import com.queryapplication.dto.CreateAdminDTO;
import com.queryapplication.entity.*;
import com.queryapplication.repository.RoleRepository;
import com.queryapplication.repository.UserRepository;
import com.queryapplication.service.impl.AdminServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

        @Mock
        private UserRepository userRepository;

        @Mock
        private RoleRepository roleRepository;

        @InjectMocks
        private AdminServiceImpl adminService;

        private Users mockUser;
        private Role mockRole;

        @BeforeEach
        public void setUp() {
                mockUser = new Users();
                mockUser.setId(1L);
                mockUser.setFirstName("John Doe");
                mockUser.setEmail("john@example.com");
                mockUser.setUsername("johndoe");
                mockUser.setStatus(Status.ACTIVE);

                mockRole = new Role();
                mockRole.setRoleName(RoleName.ADMIN);
        }

        @Test
        @DisplayName("Test Create Admin - Success")
        void givenValidDTO_whenCreateAdmin_thenReturnCreatedUser() {

                CreateAdminDTO createAdminDTO = new CreateAdminDTO();
                createAdminDTO.setFirstName("John Doe");
                createAdminDTO.setEmail("john.doe@example.com");
                createAdminDTO.setUsername("johnny123");
                createAdminDTO.setPassword("securePass123");
                createAdminDTO.setLocation(LocationName.TRIVANDRUM);
                createAdminDTO.setUserRole(RoleName.ADMIN);

                when(userRepository.existsByEmail(createAdminDTO.getEmail())).thenReturn(false);
                when(userRepository.existsByUsername(createAdminDTO.getUsername())).thenReturn(false);

                Role mockRole = new Role();
                mockRole.setRoleName(RoleName.ADMIN);

                when(roleRepository.findByRoleName(createAdminDTO.getUserRole())).thenReturn(java.util.Optional.of(mockRole));
                when(userRepository.save(any(Users.class))).thenReturn(new Users());

                Users createdUser = adminService.createAdmin(createAdminDTO);

                assertNotNull(createdUser);
                verify(userRepository).save(any(Users.class));
        }

        @Test
        @DisplayName("Test Create Admin - Email Already In Use")
        void givenEmailInUse_whenCreateAdmin_thenThrowException() {

                CreateAdminDTO createAdminDTO = new CreateAdminDTO();
                createAdminDTO.setFirstName("John Doe");
                createAdminDTO.setEmail("john.doe@example.com");
                createAdminDTO.setUsername("johnny123");
                createAdminDTO.setPassword("securePass123");
                createAdminDTO.setLocation(LocationName.TRIVANDRUM);
                createAdminDTO.setUserRole(RoleName.ADMIN);

                when(userRepository.existsByEmail(createAdminDTO.getEmail())).thenReturn(true);

                RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                    adminService.createAdmin(createAdminDTO);
                });
                assertEquals("Email already in use", exception.getMessage());
        }

        @Test
        @DisplayName("Test Create Admin - Username Already In Use")
        void givenUsernameInUse_whenCreateAdmin_thenThrowException() {

                CreateAdminDTO createAdminDTO = new CreateAdminDTO();
                createAdminDTO.setFirstName("John Doe");
                createAdminDTO.setEmail("john.doe@example.com");
                createAdminDTO.setUsername("johnny123");
                createAdminDTO.setPassword("securePass123");
                createAdminDTO.setLocation(LocationName.TRIVANDRUM);
                createAdminDTO.setUserRole(RoleName.ADMIN);

                when(userRepository.existsByEmail(createAdminDTO.getEmail())).thenReturn(false);
                when(userRepository.existsByUsername(createAdminDTO.getUsername())).thenReturn(true);

                RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                    adminService.createAdmin(createAdminDTO);
                });
                assertEquals("Username already in use", exception.getMessage());
        }

        @Test
        @DisplayName("Test Create Admin - Role Not Found")
        void givenRoleNotFound_whenCreateAdmin_thenThrowException()  {

                CreateAdminDTO createAdminDTO = new CreateAdminDTO();
                createAdminDTO.setFirstName("John Doe");
                createAdminDTO.setEmail("john.doe@example.com");
                createAdminDTO.setUsername("johnny123");
                createAdminDTO.setPassword("securePass123");
                createAdminDTO.setLocation(LocationName.TRIVANDRUM);
                createAdminDTO.setUserRole(RoleName.ADMIN);

                when(userRepository.existsByEmail(createAdminDTO.getEmail())).thenReturn(false);
                when(userRepository.existsByUsername(createAdminDTO.getUsername())).thenReturn(false);
                when(roleRepository.findByRoleName(createAdminDTO.getUserRole())).thenReturn(java.util.Optional.empty()); // Simulate role not found

                RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                    adminService.createAdmin(createAdminDTO);
                });
                assertEquals("Role not found", exception.getMessage());
        }


        @Test
        @DisplayName("Test Get All Users - Success")
        public void givenUsersInDatabase_whenGetAllUsers_thenReturnUsersList() {
                when(userRepository.findAll()).thenReturn(List.of(mockUser));
                Iterable<Users> users = adminService.getAllUsers();
                assertNotNull(users);
                assertTrue(users.iterator().hasNext());
        }

        @Test
        @DisplayName("Test Toggle Admin Status - Active to Inactive")
        public void givenActiveAdmin_whenToggleAdminStatus_thenSetStatusToInactive() {
                when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
                when(userRepository.save(any(Users.class))).thenReturn(mockUser);

                Users updatedUser = adminService.toggleAdminStatus(1L);

                assertEquals(Status.INACTIVE, updatedUser.getStatus());
                verify(userRepository, times(1)).save(updatedUser);
        }

        @Test
        @DisplayName("Test Toggle Admin Status - Inactive to Active")
        public void givenInactiveAdmin_whenToggleAdminStatus_thenSetStatusToActive() {
                mockUser.setStatus(Status.INACTIVE);
                when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
                when(userRepository.save(any(Users.class))).thenReturn(mockUser);

                Users updatedUser = adminService.toggleAdminStatus(1L);
                assertEquals(Status.ACTIVE, updatedUser.getStatus());
                verify(userRepository, times(1)).save(updatedUser);
        }

        @Test
        @DisplayName("Test Get User Details - Success")
        public void givenValidUserId_whenGetUserDetails_thenReturnUser() {
                when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

                System.out.println(mockUser);

                Users userDetails = adminService.getUserDetails(1L);

                System.out.println(userDetails);
                assertNotNull(userDetails);
                assertEquals("John Doe", userDetails.getFirstName());
        }


    @Test
        @DisplayName("Test Get User Details - User Not Found")
        public void givenInvalidUserId_whenGetUserDetails_thenThrowUserNotFoundException() {
                when(userRepository.findById(1L)).thenReturn(Optional.empty());

                Exception exception = assertThrows(RuntimeException.class, () -> {
                    adminService.getUserDetails(1L);
                });

                assertEquals("User not found", exception.getMessage());
        }


}


