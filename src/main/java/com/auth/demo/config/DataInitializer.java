package com.auth.demo.config;

import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.auth.demo.model.AdminModel;
import com.auth.demo.repository.AdminRepository;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner init(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        return args -> {

            String adminEmail = "admin@admin.com";

            // verifica se já existe
            if (adminRepository.findByEmail(adminEmail).isEmpty()) {

                AdminModel admin = new AdminModel();
                admin.setNome("Usuário Administrador");
                admin.setEmail(adminEmail);
                admin.setSenha(passwordEncoder.encode("123456"));
                admin.setRole("ROLE_ADMIN");
                admin.setCode("D1398");
                admin.setDataHoraCadastro(LocalDateTime.now());

                adminRepository.save(admin);

                System.out.println("✅ Admin criado com sucesso!");
            }
        };
    }
}
