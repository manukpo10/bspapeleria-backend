package com.bspapeleria.backend.controller;

import com.bspapeleria.backend.dto.CuponRequest;
import com.bspapeleria.backend.dto.CuponResponse;
import com.bspapeleria.backend.service.CuponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cupones")
@RequiredArgsConstructor
public class CuponController {

    private final CuponService cuponService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CuponResponse>> getAllCupones(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(cuponService.getAllCupones(PageRequest.of(page, size)));
    }

    @GetMapping("/validar")
    public ResponseEntity<CuponResponse> validarCupon(
            @RequestParam String codigo,
            @RequestParam(defaultValue = "0") Double montoTotal) {
        return ResponseEntity.ok(cuponService.validarCupon(codigo, montoTotal));
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<CuponResponse> getCuponByCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(cuponService.getCuponByCodigo(codigo));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CuponResponse> createCupon(@Valid @RequestBody CuponRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cuponService.createCupon(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CuponResponse> updateCupon(
            @PathVariable Long id,
            @Valid @RequestBody CuponRequest request) {
        return ResponseEntity.ok(cuponService.updateCupon(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCupon(@PathVariable Long id) {
        cuponService.deleteCupon(id);
        return ResponseEntity.noContent().build();
    }
}