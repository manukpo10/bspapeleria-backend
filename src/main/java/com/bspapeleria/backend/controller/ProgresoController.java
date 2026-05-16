package com.bspapeleria.backend.controller;

import com.bspapeleria.backend.dto.ProgresoRequest;
import com.bspapeleria.backend.dto.ProgresoResponse;
import com.bspapeleria.backend.entity.Usuario;
import com.bspapeleria.backend.repository.UsuarioRepository;
import com.bspapeleria.backend.service.ProgresoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/progreso")
@RequiredArgsConstructor
public class ProgresoController {

    private final ProgresoService progresoService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<List<ProgresoResponse>> getMyProgresos(@AuthenticationPrincipal UserDetails userDetails) {
        Long usuarioId = extractUsuarioId(userDetails);
        return ResponseEntity.ok(progresoService.getProgresosByUsuario(usuarioId));
    }

    @GetMapping("/{cursoId}")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<ProgresoResponse> getMiProgresoDelCurso(
            @PathVariable Long cursoId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long usuarioId = extractUsuarioId(userDetails);
        return ResponseEntity.ok(progresoService.getProgresoByUsuarioAndCurso(usuarioId, cursoId));
    }

    @PostMapping("/{cursoId}/inscribirse")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<ProgresoResponse> inscribirse(
            @PathVariable Long cursoId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long usuarioId = extractUsuarioId(userDetails);
        return ResponseEntity.ok(progresoService.inscribirUsuario(usuarioId, cursoId));
    }

    @PutMapping("/{cursoId}/leccion/{leccionId}")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<ProgresoResponse> marcarLeccionCompletada(
            @PathVariable Long cursoId,
            @PathVariable Long leccionId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long usuarioId = extractUsuarioId(userDetails);
        return ResponseEntity.ok(progresoService.marcarLeccionCompletada(usuarioId, cursoId, leccionId));
    }

    @PutMapping("/{cursoId}")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<ProgresoResponse> updateProgreso(
            @PathVariable Long cursoId,
            @RequestBody ProgresoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long usuarioId = extractUsuarioId(userDetails);
        return ResponseEntity.ok(progresoService.updateProgreso(usuarioId, cursoId, request));
    }

    private Long extractUsuarioId(UserDetails userDetails) {
        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + userDetails.getUsername()));
        return usuario.getId();
    }
}