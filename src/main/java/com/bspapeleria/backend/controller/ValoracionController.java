package com.bspapeleria.backend.controller;

import com.bspapeleria.backend.dto.ValoracionRequest;
import com.bspapeleria.backend.dto.ValoracionResponse;
import com.bspapeleria.backend.entity.Usuario;
import com.bspapeleria.backend.repository.UsuarioRepository;
import com.bspapeleria.backend.service.ValoracionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/valoraciones")
@RequiredArgsConstructor
public class ValoracionController {

    private final ValoracionService valoracionService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping
    public ResponseEntity<Page<ValoracionResponse>> getValoraciones(
            @RequestParam String entidadTipo,
            @RequestParam Long entidadId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(valoracionService.getValoracionesByEntidad(
                entidadTipo, entidadId, PageRequest.of(page, size)));
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<ValoracionResponse> createValoracion(
            @Valid @RequestBody ValoracionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long usuarioId = extractUsuarioId(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(valoracionService.createValoracion(usuarioId, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<Void> deleteValoracion(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long usuarioId = extractUsuarioId(userDetails);
        valoracionService.deleteValoracion(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    private Long extractUsuarioId(UserDetails userDetails) {
        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + userDetails.getUsername()));
        return usuario.getId();
    }
}