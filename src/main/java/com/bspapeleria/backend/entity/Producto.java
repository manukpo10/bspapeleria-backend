package com.bspapeleria.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(length = 500)
    private String descripcionCorta;

    @Column(nullable = false)
    private Double precio;

    private Double precioComparacion;

    @ElementCollection
    @CollectionTable(name = "producto_imagenes", joinColumns = @JoinColumn(name = "producto_id"))
    @Column(name = "imagen")
    private List<String> imagenes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Categoria categoria;

    @ElementCollection
    @CollectionTable(name = "producto_tags", joinColumns = @JoinColumn(name = "producto_id"))
    @Column(name = "tag")
    private List<String> tags;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(nullable = false)
    private Boolean esDigital = false;

    private String urlDescarga;

    @Column(nullable = false)
    private Boolean destacado = false;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false)
    private Double rating = 0.0;

    @Column(nullable = false)
    private Integer reviewsCount = 0;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (slug == null || slug.isEmpty()) {
            slug = nombre.toLowerCase().replaceAll("\\s+", "-").replaceAll("[^a-z0-9-]", "");
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public enum Categoria {
        personalizados, sublimables, fiestas, carteleria, archivos_digitales
    }
}