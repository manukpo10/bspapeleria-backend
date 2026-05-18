package com.bspapeleria.backend.controller;

import com.bspapeleria.backend.dto.CursoRequest;
import com.bspapeleria.backend.dto.CursoResponse;
import com.bspapeleria.backend.dto.UsuarioResponse;
import com.bspapeleria.backend.entity.Orden;
import com.bspapeleria.backend.entity.Usuario;
import com.bspapeleria.backend.exception.BadRequestException;
import com.bspapeleria.backend.repository.*;
import com.bspapeleria.backend.service.CursoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UsuarioRepository usuarioRepository;
    private final OrdenRepository ordenRepository;
    private final CursoRepository cursoRepository;
    private final CursoService cursoService;

    @GetMapping("/usuarios")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UsuarioResponse>> getAllUsuarios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(usuarioRepository.findAll(PageRequest.of(page, size))
                .map(this::toResponse));
    }

    @PutMapping("/usuarios/{id}/rol")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> updateUserRole(
            @PathVariable Long id,
            @RequestParam String rol) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + id));

        String rolUpper = rol.toUpperCase();
        if (Arrays.stream(Usuario.Rol.values()).noneMatch(r -> r.name().equals(rolUpper))) {
            throw new BadRequestException("Rol inválido. Valores válidos: ADMIN, CLIENTE");
        }

        usuario.setRol(Usuario.Rol.valueOf(rolUpper));
        usuarioRepository.save(usuario);
        return ResponseEntity.ok(toResponse(usuario));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public ResponseEntity<java.util.Map<String, Object>> getAdminStats() {
        List<Orden> ordenes = ordenRepository.findAll();
        double totalSales = ordenes.stream()
                .filter(o -> o.getEstado() == Orden.Estado.ENTREGADA || o.getEstado() == Orden.Estado.CONFIRMADA)
                .mapToDouble(Orden::getTotal)
                .sum();

        long clientesCount = usuarioRepository.findAll().stream()
                .filter(u -> u.getRol() == Usuario.Rol.CLIENTE)
                .count();

        long activeCourses = cursoRepository.findAll().stream()
                .filter(c -> c.getActivo())
                .count();

        long productsSold = ordenes.stream()
                .filter(o -> o.getDetalles() != null)
                .mapToLong(o -> o.getDetalles().stream()
                        .filter(d -> d.getTipoItem() == com.bspapeleria.backend.entity.OrdenDetalle.TipoItem.PRODUCTO)
                        .count())
                .sum();

        return ResponseEntity.ok(java.util.Map.of(
                "totalSales", totalSales,
                "newStudents", clientesCount,
                "activeCourses", activeCourses,
                "productsSold", productsSold
        ));
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .email(usuario.getEmail())
                .telefono(usuario.getTelefono())
                .rol(usuario.getRol().name())
                .activo(usuario.getActivo())
                .fechaCreacion(usuario.getFechaCreacion())
                .build();
    }

    @PostMapping("/cursos/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CursoResponse> createCursoWithLecciones(@RequestBody CursoRequest request) {
        return ResponseEntity.ok(cursoService.createCurso(request));
    }
}