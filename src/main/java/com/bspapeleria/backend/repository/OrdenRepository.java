package com.bspapeleria.backend.repository;

import com.bspapeleria.backend.entity.Orden;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OrdenRepository extends JpaRepository<Orden, Long> {

    Optional<Orden> findByNumeroOrden(String numeroOrden);

    Page<Orden> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId, Pageable pageable);

    @Query("SELECT o FROM Orden o WHERE o.usuario.id = :usuarioId AND o.estado = :estado ORDER BY o.fechaCreacion DESC")
    Page<Orden> findByUsuarioIdAndEstado(@Param("usuarioId") Long usuarioId, @Param("estado") Orden.Estado estado, Pageable pageable);

    Page<Orden> findByEstadoOrderByFechaCreacionDesc(Orden.Estado estado, Pageable pageable);

    @Query("SELECT COUNT(o) FROM Orden o WHERE o.usuario.id = :usuarioId AND o.estadoPago = 'APROBADO'")
    long countCompletedOrdersByUsuario(@Param("usuarioId") Long usuarioId);

    Optional<Orden> findByMercadoPagoPreferenceId(String preferenceId);
}