package com.bspapeleria.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UsuarioResponse {
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String rol;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private List<EnrollmentResponse> enrollments;
}