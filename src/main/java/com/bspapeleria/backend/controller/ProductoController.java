package com.bspapeleria.backend.controller;

import com.bspapeleria.backend.dto.ProductoRequest;
import com.bspapeleria.backend.dto.ProductoResponse;
import com.bspapeleria.backend.service.ProductoService;
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
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    public ResponseEntity<Page<ProductoResponse>> getAllProductos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(defaultValue = "newest") String sortBy) {

        Sort sort = switch (sortBy) {
            case "price-asc" -> Sort.by("precio").ascending();
            case "price-desc" -> Sort.by("precio").descending();
            case "rating" -> Sort.by("rating").descending();
            default -> Sort.by("fechaCreacion").descending();
        };

        PageRequest pageable = PageRequest.of(page, size, sort);

        Page<ProductoResponse> productos;
        productos = productoService.searchProductos(search, categories, minPrice, maxPrice, pageable);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(2, TimeUnit.MINUTES).cachePublic())
                .body(productos);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ProductoResponse> getProductoBySlug(@PathVariable String slug) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(productoService.getProductoBySlug(slug));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<ProductoResponse> getProductoById(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.getProductoById(id));
    }

    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<Page<ProductoResponse>> getProductosByCategoria(
            @PathVariable String categoria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("fechaCreacion").descending());
        return ResponseEntity.ok(productoService.getProductosByCategoria(categoria, pageable));
    }

    @GetMapping("/destacados")
    public ResponseEntity<Page<ProductoResponse>> getProductosDestacados(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productoService.getAllProductos(PageRequest.of(page, size)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductoResponse> createProducto(@Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productoService.createProducto(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductoResponse> updateProducto(
            @PathVariable Long id,
            @Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.ok(productoService.updateProducto(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProducto(@PathVariable Long id) {
        productoService.deleteProducto(id);
        return ResponseEntity.noContent().build();
    }
}