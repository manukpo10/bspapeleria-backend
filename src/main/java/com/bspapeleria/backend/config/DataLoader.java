package com.bspapeleria.backend.config;

import com.bspapeleria.backend.entity.Usuario;
import com.bspapeleria.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!usuarioRepository.existsByEmail("admin@bspapeleria.com")) {
            usuarioRepository.save(Usuario.builder()
                    .email("admin@bspapeleria.com")
                    .password(passwordEncoder.encode("admin123"))
                    .nombre("Admin")
                    .apellido("BS")
                    .telefono("+5491112345678")
                    .rol(Usuario.Rol.ADMIN)
                    .activo(true)
                    .build());
        }

        if (!usuarioRepository.existsByEmail("profesor@bspapeleria.com")) {
            usuarioRepository.save(Usuario.builder()
                    .email("profesor@bspapeleria.com")
                    .password(passwordEncoder.encode("profesor123"))
                    .nombre("Profesor")
                    .apellido("Demo")
                    .telefono("+5491112345679")
                    .rol(Usuario.Rol.CLIENTE)
                    .activo(true)
                    .build());
        }

        if (!usuarioRepository.existsByEmail("alumno@bspapeleria.com")) {
            usuarioRepository.save(Usuario.builder()
                    .email("alumno@bspapeleria.com")
                    .password(passwordEncoder.encode("alumno123"))
                    .nombre("Alumno")
                    .apellido("Demo")
                    .telefono("+5491112345680")
                    .rol(Usuario.Rol.CLIENTE)
                    .activo(true)
                    .build());
        }

        System.out.println("=== Seed Data Loaded ===");
        System.out.println("Admin:     admin@bspapeleria.com / admin123");
        System.out.println("Profesor:  profesor@bspapeleria.com / profesor123");
        System.out.println("Alumno:    alumno@bspapeleria.com / alumno123");
    }
}