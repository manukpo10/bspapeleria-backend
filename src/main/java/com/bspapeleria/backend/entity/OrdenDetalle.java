package com.bspapeleria.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "orden_detalles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_id", nullable = false)
    private Orden orden;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoItem tipoItem;

    @Column(nullable = false)
    private String itemId;

    private String nombre;

    private String imagenUrl;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    private Double precioUnitario;

    @Column(nullable = false)
    private Double subtotal;

    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (subtotal == null && cantidad != null && precioUnitario != null) {
            subtotal = cantidad * precioUnitario;
        }
    }

    public enum TipoItem {
        PRODUCTO, CURSO
    }
}