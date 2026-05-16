package com.bspapeleria.backend.controller;

import com.bspapeleria.backend.dto.OrdenRequest;
import com.bspapeleria.backend.dto.OrdenResponse;
import com.bspapeleria.backend.entity.Usuario;
import com.bspapeleria.backend.repository.UsuarioRepository;
import com.bspapeleria.backend.service.OrdenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ordenes")
@RequiredArgsConstructor
public class OrdenController {

    private final OrdenService ordenService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<Page<OrdenResponse>> getMisOrdenes(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long usuarioId = extractUsuarioId(userDetails);
        return ResponseEntity.ok(ordenService.getOrdenesByUsuario(usuarioId, PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<OrdenResponse> getMiOrden(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long usuarioId = extractUsuarioId(userDetails);
        return ResponseEntity.ok(ordenService.getOrdenById(id, usuarioId));
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<OrdenResponse> createOrden(
            @Valid @RequestBody OrdenRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long usuarioId = extractUsuarioId(userDetails);
        return ResponseEntity.ok(ordenService.createOrden(usuarioId, request));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrdenResponse>> getAllOrdenes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String estado) {
        if (estado != null && !estado.isEmpty()) {
            return ResponseEntity.ok(ordenService.getOrdenesByEstado(estado, PageRequest.of(page, size)));
        }
        return ResponseEntity.ok(ordenService.getAllOrdenes(PageRequest.of(page, size)));
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrdenResponse> updateOrdenEstado(
            @PathVariable Long id,
            @RequestParam String estado) {
        return ResponseEntity.ok(ordenService.updateOrdenEstado(id, estado));
    }

    private Long extractUsuarioId(UserDetails userDetails) {
        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        return usuario.getId();
    }
}