package com.bspapeleria.backend.controller;

import com.bspapeleria.backend.dto.CarritoItemRequest;
import com.bspapeleria.backend.dto.CarritoResponse;
import com.bspapeleria.backend.entity.Usuario;
import com.bspapeleria.backend.repository.UsuarioRepository;
import com.bspapeleria.backend.service.CarritoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carrito")
@RequiredArgsConstructor
public class CarritoController {

    private final CarritoService carritoService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<CarritoResponse> getMiCarrito(@AuthenticationPrincipal UserDetails userDetails) {
        Long usuarioId = extractUsuarioId(userDetails);
        return ResponseEntity.ok(carritoService.getCarritoByUsuario(usuarioId));
    }

    @PostMapping("/items")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<CarritoResponse> agregarItem(
            @Valid @RequestBody CarritoItemRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long usuarioId = extractUsuarioId(userDetails);
        return ResponseEntity.ok(carritoService.agregarItem(usuarioId, request));
    }

    @PutMapping("/items")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<CarritoResponse> actualizarItem(
            @Valid @RequestBody CarritoItemRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long usuarioId = extractUsuarioId(userDetails);
        return ResponseEntity.ok(carritoService.actualizarItem(usuarioId, request));
    }

    @DeleteMapping("/items/{tipo}/{itemId}")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<CarritoResponse> eliminarItem(
            @PathVariable String tipo,
            @PathVariable String itemId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long usuarioId = extractUsuarioId(userDetails);
        return ResponseEntity.ok(carritoService.eliminarItem(usuarioId, tipo, itemId));
    }

    @DeleteMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<Void> vaciarCarrito(@AuthenticationPrincipal UserDetails userDetails) {
        Long usuarioId = extractUsuarioId(userDetails);
        carritoService.vaciarCarrito(usuarioId);
        return ResponseEntity.noContent().build();
    }

    private Long extractUsuarioId(UserDetails userDetails) {
        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + userDetails.getUsername()));
        return usuario.getId();
    }
}