package com.bspapeleria.backend.service;

import com.bspapeleria.backend.dto.ProductoRequest;
import com.bspapeleria.backend.dto.ProductoResponse;
import com.bspapeleria.backend.entity.Producto;
import com.bspapeleria.backend.entity.Producto.Categoria;
import com.bspapeleria.backend.exception.BadRequestException;
import com.bspapeleria.backend.exception.ResourceNotFoundException;
import com.bspapeleria.backend.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageImpl;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public Page<ProductoResponse> getAllProductos(Pageable pageable) {
        List<Producto> all = productoRepository.findAllActivosWithCollections();
        List<ProductoResponse> responses = all.stream().map(this::toResponse).collect(Collectors.toList());
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), responses.size());
        List<ProductoResponse> page = start >= responses.size() ? List.of() : responses.subList(start, end);
        return new PageImpl<>(page, pageable, responses.size());
    }

    @Transactional(readOnly = true)
    public ProductoResponse getProductoBySlug(String slug) {
        Producto producto = productoRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + slug));
        return toResponse(producto);
    }

    @Transactional(readOnly = true)
    public ProductoResponse getProductoById(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + id));
        return toResponse(producto);
    }

    @Transactional(readOnly = true)
    public Page<ProductoResponse> getProductosByCategoria(String categoria, Pageable pageable) {
        Categoria cat;
        try {
            cat = Categoria.valueOf(categoria.toLowerCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Categoría inválida: " + categoria);
        }
        return productoRepository.findByCategoriaAndActivoTrue(cat, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductoResponse> searchProductos(String search, List<String> categorias, Double minPrice, Double maxPrice, Pageable pageable) {
        List<Categoria> cats = null;
        if (categorias != null && !categorias.isEmpty()) {
            cats = categorias.stream()
                    .map(c -> {
                        try {
                            return Categoria.valueOf(c.toLowerCase());
                        } catch (IllegalArgumentException e) {
                            return null;
                        }
                    })
                    .filter(c -> c != null)
                    .collect(Collectors.toList());
        }

        // If no filters at all, return all active products (avoids null parameter type issues with PostgreSQL)
        if ((search == null || search.isEmpty()) && (cats == null || cats.isEmpty()) && minPrice == null && maxPrice == null) {
            return productoRepository.findByActivoTrue(pageable).map(this::toResponse);
        }

        // Ensure search is empty string if null, to avoid PostgreSQL type inference issues
        String safeSearch = search == null ? "" : search;

        Page<Producto> resultados = productoRepository.searchWithFilters(safeSearch, cats, minPrice, maxPrice, pageable);
        return resultados.map(this::toResponse);
    }

    @Transactional
    public ProductoResponse createProducto(ProductoRequest request) {
        Producto producto = Producto.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .descripcionCorta(request.getDescripcionCorta())
                .precio(request.getPrecio())
                .precioComparacion(request.getPrecioComparacion())
                .imagenes(request.getImagenes() != null ? new java.util.HashSet<>(request.getImagenes()) : null)
                .categoria(parseCategoria(request.getCategoria()))
                .tags(request.getTags() != null ? new java.util.HashSet<>(request.getTags()) : null)
                .stock(request.getStock())
                .esDigital(request.getEsDigital() != null ? request.getEsDigital() : false)
                .urlDescarga(request.getUrlDescarga())
                .destacado(request.getDestacado() != null ? request.getDestacado() : false)
                .rating(0.0)
                .reviewsCount(0)
                .build();

        productoRepository.save(producto);
        return toResponse(producto);
    }

    @Transactional
    public ProductoResponse updateProducto(Long id, ProductoRequest request) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + id));

        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setDescripcionCorta(request.getDescripcionCorta());
        producto.setPrecio(request.getPrecio());
        producto.setPrecioComparacion(request.getPrecioComparacion());
        producto.setImagenes(request.getImagenes() != null ? new java.util.HashSet<>(request.getImagenes()) : null);
        producto.setCategoria(parseCategoria(request.getCategoria()));
        producto.setTags(request.getTags() != null ? new java.util.HashSet<>(request.getTags()) : null);
        producto.setStock(request.getStock());
        producto.setEsDigital(request.getEsDigital() != null ? request.getEsDigital() : false);
        producto.setUrlDescarga(request.getUrlDescarga());
        producto.setDestacado(request.getDestacado() != null ? request.getDestacado() : false);

        productoRepository.save(producto);
        return toResponse(producto);
    }

    @Transactional
    public void deleteProducto(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Producto no encontrado: " + id);
        }
        productoRepository.deleteById(id);
    }

    private Categoria parseCategoria(String categoria) {
        if (categoria == null || categoria.isEmpty()) {
            return Categoria.personalizados;
        }
        String normalized = categoria.toLowerCase().replace(" ", "_").replace("-", "_");
        try {
            return Categoria.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return Categoria.personalizados;
        }
    }

    private ProductoResponse toResponse(Producto producto) {
        return ProductoResponse.builder()
                .id(producto.getId())
                .slug(producto.getSlug())
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .descripcionCorta(producto.getDescripcionCorta())
                .precio(producto.getPrecio())
                .precioComparacion(producto.getPrecioComparacion())
                .imagenes(producto.getImagenes() != null ? new java.util.ArrayList<>(producto.getImagenes()) : null)
                .categoria(producto.getCategoria().name())
                .tags(producto.getTags() != null ? new java.util.ArrayList<>(producto.getTags()) : null)
                .stock(producto.getStock())
                .esDigital(producto.getEsDigital())
                .urlDescarga(producto.getUrlDescarga())
                .destacado(producto.getDestacado())
                .rating(producto.getRating())
                .reviewsCount(producto.getReviewsCount())
                .createdAt(producto.getFechaCreacion())
                .build();
    }
}