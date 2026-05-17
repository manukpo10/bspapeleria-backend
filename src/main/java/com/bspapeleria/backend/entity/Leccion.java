package com.bspapeleria.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "lecciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Leccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String contenido;

    private String urlVideo;

    private String urlMaterial;

    @Column(nullable = false)
    private Integer orden = 0;

    @Column(nullable = false)
    private Integer duracionMinutos = 0;

    @Column(nullable = false)
    private Boolean esPreview = false;

    @PrePersist
    protected void onCreate() {
        if (esPreview == null) esPreview = false;
        if (orden == null) orden = 0;
        if (duracionMinutos == null) duracionMinutos = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Leccion that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}