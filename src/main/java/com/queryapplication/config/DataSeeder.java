package com.queryapplication.config;

import com.queryapplication.dto.CreateAdminDTO;
import com.queryapplication.entity.LocationName;
import com.queryapplication.entity.Role;
import com.queryapplication.entity.RoleName;
import com.queryapplication.repository.RoleRepository;
import com.queryapplication.repository.UserRepository;
import com.queryapplication.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final AdminService adminService;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    public DataSeeder(RoleRepository roleRepository, UserRepository userRepository, AdminService adminService) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.adminService = adminService;
    }


    @Override
    public void run(String... args) throws Exception {
        seedRoles();
        seedAdmin();
    }

    private void seedRoles() {
        for (RoleName roleName : RoleName.values()){
            roleRepository.findByRoleName(roleName)
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setRoleName(roleName);
                        log.info("Seeding role: {}", roleName);
                        return roleRepository.save(role);
                    });
        }
    }

    private void seedAdmin() {
        if(userRepository.existsByEmail(adminEmail)){
            log.info("Admin user already exists, skipping seeding.");
            return;
        }

        CreateAdminDTO createAdminDTO = new CreateAdminDTO();
        createAdminDTO.setEmail(adminEmail);
        createAdminDTO.setPassword(adminPassword);
        createAdminDTO.setFirstName("Uno");
        createAdminDTO.setUsername("unoSuperAdmin");
        createAdminDTO.setUserRole(RoleName.SUPER_ADMIN);
        createAdminDTO.setLocation(LocationName.valueOf("KOCHI"));

        adminService.createAdmin(createAdminDTO);
        log.info("Created initial admin user with email: {}", adminEmail);
    }
}
