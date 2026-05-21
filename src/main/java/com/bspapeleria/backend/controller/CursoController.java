package com.bspapeleria.backend.controller;

import com.bspapeleria.backend.dto.CursoRequest;
import com.bspapeleria.backend.dto.CursoResponse;
import com.bspapeleria.backend.service.CursoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/cursos")
@RequiredArgsConstructor
public class CursoController {

    private final CursoService cursoService;

    @GetMapping
    public ResponseEntity<Page<CursoResponse>> getAllCursos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String nivel,
            @RequestParam(required = false) String modalidad,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "newest") String sortBy) {

        Sort sort = switch (sortBy) {
            case "price-asc" -> Sort.by("precio").ascending();
            case "price-desc" -> Sort.by("precio").descending();
            case "rating" -> Sort.by("rating").descending();
            default -> Sort.by("fechaCreacion").descending();
        };

        PageRequest pageable = PageRequest.of(page, size, sort);

        Page<CursoResponse> cursos;
        if (search != null && !search.isEmpty()) {
            cursos = cursoService.searchCursos(search, pageable);
        } else if (nivel != null && !nivel.isEmpty()) {
            cursos = cursoService.getCursosByNivel(nivel, pageable);
        } else if (modalidad != null && !modalidad.isEmpty()) {
            cursos = cursoService.getCursosByModalidad(modalidad, pageable);
        } else if (minPrice != null || maxPrice != null) {
            cursos = cursoService.getCursosByPrecioRange(minPrice, maxPrice, pageable);
        } else {
            cursos = cursoService.getAllCursos(pageable);
        }

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(2, TimeUnit.MINUTES).cachePublic())
                .body(cursos);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<CursoResponse> getCursoBySlug(@PathVariable String slug) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(cursoService.getCursoBySlug(slug));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<CursoResponse> getCursoById(@PathVariable Long id) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(cursoService.getCursoById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CursoResponse> createCurso(@Valid @RequestBody CursoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cursoService.createCurso(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CursoResponse> updateCurso(
            @PathVariable Long id,
            @Valid @RequestBody CursoRequest request) {
        return ResponseEntity.ok(cursoService.updateCurso(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCurso(@PathVariable Long id) {
        cursoService.deleteCurso(id);
        return ResponseEntity.noContent().build();
    }
}