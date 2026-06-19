package com.upeu.auth.config;

import com.upeu.auth.entity.AuthUser;
import com.upeu.auth.entity.Role;
import com.upeu.auth.repository.AuthUserRepository;
import com.upeu.auth.repository.RoleRepository;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initAuthData() {
        return args -> {
            Role adminRole = ensureRole("ADMIN");
            Role userRole = ensureRole("USER");
            Role cajeroRole = ensureRole("CAJERO");
            Role supervisorRole = ensureRole("SUPERVISOR");
            Role repartidorRole = ensureRole("REPARTIDOR");

            ensureUser("admin", "admin123", Set.of(adminRole));
            ensureUser("user", "user123", Set.of(userRole));
            ensureUser("cajero", "cajero123", Set.of(cajeroRole));
            ensureUser("supervisor", "supervisor123", Set.of(supervisorRole));
            ensureUser("repartidor", "repartidor123", Set.of(repartidorRole));
        };
    }

    private Role ensureRole(String name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(Role.builder().name(name).build()));
    }

    /** Crea el usuario o actualiza la contraseña (útil si la BD ya tenía datos viejos). */
    private void ensureUser(String username, String rawPassword, Set<Role> roles) {
        authUserRepository.findByUsername(username)
                .ifPresentOrElse(user -> {
                    user.setPassword(passwordEncoder.encode(rawPassword));
                    user.setEnabled(true);
                    user.setRoles(roles);
                    authUserRepository.save(user);
                }, () -> authUserRepository.save(AuthUser.builder()
                        .username(username)
                        .password(passwordEncoder.encode(rawPassword))
                        .enabled(true)
                        .roles(roles)
                        .build()));
    }
}
