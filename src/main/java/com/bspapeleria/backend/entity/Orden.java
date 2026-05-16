package com.bspapeleria.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ordenes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Orden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String numeroOrden;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado;

    @Column(nullable = false)
    private Double subtotal = 0.0;

    @Column(nullable = false)
    private Double descuento = 0.0;

    @Column(nullable = false)
    private Double costoEnvio = 0.0;

    @Column(nullable = false)
    private Double total = 0.0;

    @Enumerated(EnumType.STRING)
    private MetodoPago metodoPago;

    @Enumerated(EnumType.STRING)
    private EstadoPago estadoPago;

    private String mercadoPagoPreferenceId;

    private String mercadoPagoPaymentId;

    private String mercadoPagoMerchantOrderId;

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrdenDetalle> detalles = new ArrayList<>();

    @Embedded
    private DireccionEnvio direccionEnvio;

    private String notas;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
        if (estado == null) estado = Estado.PENDIENTE;
        if (estadoPago == null) estadoPago = EstadoPago.PENDIENTE;
        if (subtotal == null) subtotal = 0.0;
        if (descuento == null) descuento = 0.0;
        if (costoEnvio == null) costoEnvio = 0.0;
        if (total == null) total = 0.0;
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public enum Estado {
        PENDIENTE, CONFIRMADA, ENVIADA, ENTREGADA, CANCELADA
    }

    public enum MetodoPago {
        MERCADO_PAGO, TRANSFERENCIA, EFECTIVO
    }

    public enum EstadoPago {
        PENDIENTE, APROBADO, RECHAZADO, REEMBOLSADO
    }

    @Embeddable
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DireccionEnvio {
        private String calle;
        private String numero;
        private String piso;
        private String departamento;
        private String ciudad;
        private String provincia;
        private String codigoPostal;
        private String telefono;
        private String instrucciones;
    }
}