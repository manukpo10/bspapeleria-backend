package com.bspapeleria.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "progresos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Progreso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @ElementCollection
    @CollectionTable(name = "progreso_lecciones", joinColumns = @JoinColumn(name = "progreso_id"))
    @Column(name = "leccion_id")
    @Builder.Default
    private java.util.List<Long> leccionesCompletadas = new java.util.ArrayList<>();

    @Column(nullable = false)
    private Integer porcentajeProgreso = 0;

    private Integer leccionActualId;

    private LocalDateTime ultimaActividad;

    private LocalDateTime fechaInscripcion;

    @Column(nullable = false)
    private Boolean completado = false;

    private LocalDateTime fechaCompletado;

    private Boolean certificadoDesbloqueado = false;

    @PrePersist
    protected void onCreate() {
        fechaInscripcion = LocalDateTime.now();
        ultimaActividad = LocalDateTime.now();
        if (completado == null) completado = false;
        if (porcentajeProgreso == null) porcentajeProgreso = 0;
        if (certificadoDesbloqueado == null) certificadoDesbloqueado = false;
    }
}