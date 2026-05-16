package com.bspapeleria.backend.service;

import com.bspapeleria.backend.dto.AuthResponse;
import com.bspapeleria.backend.dto.EnrollmentResponse;
import com.bspapeleria.backend.dto.LoginRequest;
import com.bspapeleria.backend.dto.RegisterRequest;
import com.bspapeleria.backend.dto.UsuarioResponse;
import com.bspapeleria.backend.entity.Usuario;
import com.bspapeleria.backend.exception.BadRequestException;
import com.bspapeleria.backend.repository.ProgresoRepository;
import com.bspapeleria.backend.repository.UsuarioRepository;
import com.bspapeleria.backend.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final ProgresoRepository progresoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("El email ya está registrado");
        }

        Usuario.Rol rol = Usuario.Rol.CLIENTE;
        if (request.getRol() != null) {
            try {
                rol = Usuario.Rol.valueOf(request.getRol().toUpperCase());
                if (rol != Usuario.Rol.ADMIN && rol != Usuario.Rol.CLIENTE) {
                    rol = Usuario.Rol.CLIENTE;
                }
            } catch (IllegalArgumentException ignored) {}
        }
        if (rol == null) rol = Usuario.Rol.CLIENTE;

        Usuario usuario = Usuario.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .telefono(request.getTelefono())
                .rol(rol)
                .activo(true)
                .build();

        usuarioRepository.save(usuario);

        String token = jwtUtils.generateToken(usuario.getEmail(), usuario.getRol().name());

        return AuthResponse.builder()
                .token(token)
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .rol(usuario.getRol().name())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Credenciales inválidas"));

        String token = jwtUtils.generateToken(usuario.getEmail(), usuario.getRol().name());

        return AuthResponse.builder()
                .token(token)
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .rol(usuario.getRol().name())
                .build();
    }

    public UsuarioResponse getCurrentUser(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado"));
        return toResponse(usuario);
    }

    private EnrollmentResponse toEnrollmentResponse(com.bspapeleria.backend.entity.Progreso progreso) {
        return EnrollmentResponse.builder()
                .courseId(progreso.getCurso().getId())
                .courseTitle(progreso.getCurso().getTitulo())
                .completedLessons(progreso.getLeccionesCompletadas())
                .progress(progreso.getPorcentajeProgreso())
                .currentLessonId(progreso.getLeccionActualId())
                .lastAccessedAt(progreso.getUltimaActividad())
                .enrolledAt(progreso.getFechaInscripcion())
                .completed(progreso.getCompletado())
                .completedAt(progreso.getFechaCompletado())
                .certificateUnlocked(progreso.getCertificadoDesbloqueado())
                .build();
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        List<com.bspapeleria.backend.entity.Progreso> progresos = progresoRepository.findByUsuarioId(usuario.getId());
        List<EnrollmentResponse> enrollments = progresos.stream()
                .map(this::toEnrollmentResponse)
                .collect(java.util.stream.Collectors.toList());

        return UsuarioResponse.builder()
                .id(usuario.getId())
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .telefono(usuario.getTelefono())
                .rol(usuario.getRol().name())
                .activo(usuario.getActivo())
                .fechaCreacion(usuario.getFechaCreacion())
                .enrollments(enrollments)
                .build();
    }
}