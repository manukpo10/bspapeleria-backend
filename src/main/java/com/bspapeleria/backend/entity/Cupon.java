package com.bspapeleria.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cupones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDescuento tipoDescuento;

    @Column(nullable = false)
    private Double valorDescuento;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaVencimiento;

    @Column(nullable = false)
    private Integer maxUsos = 1;

    @Column(nullable = false)
    private Integer usosCount = 0;

    private Double descuentoMinimo;

    private Double descuentoMaximo;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (activo == null) activo = true;
        if (usosCount == null) usosCount = 0;
        if (maxUsos == null) maxUsos = 1;
    }

    public enum TipoDescuento {
        PORCENTAJE, MONTO_FIJO
    }
}