package com.bspapeleria.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.LinkedHashSet;

@Entity
@Table(name = "cursos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(columnDefinition = "TEXT")
    private String imagenUrl;

    @Column(nullable = false)
    private Double precio;

    private Double precioComparacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Nivel nivel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Modalidad modalidad;

    private String instructor;

    private Integer duracionHoras;

    private String urlVideoIntro;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false)
    private Integer estudiantesCount = 0;

    @Column(nullable = false)
    private Double rating = 0.0;

    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orden ASC")
    @Builder.Default
    private List<Modulo> modulos = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "curso_tags", joinColumns = @JoinColumn(name = "curso_id"))
    @Column(name = "tag")
    @Builder.Default
    private Set<String> tags = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "curso_materiales", joinColumns = @JoinColumn(name = "curso_id"))
    @Column(name = "url_material")
    @Builder.Default
    private List<String> materialUrls = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (slug == null || slug.isEmpty()) {
            slug = titulo.toLowerCase().replaceAll("\\s+", "-").replaceAll("[^a-z0-9-]", "");
        }
        if (activo == null) activo = true;
        if (estudiantesCount == null) estudiantesCount = 0;
        if (rating == null) rating = 0.0;
        if (materialUrls == null) materialUrls = new ArrayList<>();
        if (modulos == null) modulos = new ArrayList<>();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Curso that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public enum Nivel {
        principiante, intermedio, avanzado
    }

    public enum Modalidad {
        video, texto, mixto
    }
}