package com.papeleria.auth.config;

import com.papeleria.auth.model.Role;
import com.papeleria.auth.model.UserAccount;
import com.papeleria.auth.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminUserSeeder {

    @Bean
    public CommandLineRunner seedAdminUser(
            UserAccountRepository repository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.email}") String adminEmail,
            @Value("${app.admin.password}") String adminPassword,
            @Value("${app.admin.full-name}") String adminFullName
    ) {
        return args -> {
            if (repository.existsByEmail(adminEmail.toLowerCase().trim())) {
                return;
            }
            UserAccount admin = new UserAccount();
            admin.setFullName(adminFullName);
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);
            repository.save(admin);
        };
    }
}
