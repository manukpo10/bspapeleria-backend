package com.bspapeleria.backend.repository;

import com.bspapeleria.backend.entity.Cupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CuponRepository extends JpaRepository<Cupon, Long> {

    Optional<Cupon> findByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);

    /**
     * Counts one use of the coupon in a single SQL statement, so two concurrent orders can never both take the
     * last remaining use. Returns the number of rows updated: 0 means the coupon has no uses left (or no longer
     * exists). Must run inside the caller's transaction (it is a bulk update, so it bypasses the persistence
     * context: an already loaded {@link Cupon} keeps its stale {@code usosCount}).
     */
    @Modifying
    @Query("update Cupon c set c.usosCount = c.usosCount + 1 "
            + "where c.id = :id and (c.maxUsos is null or c.usosCount < c.maxUsos)")
    int incrementarUsosSiDisponible(@Param("id") Long id);
}
