package com.bspapeleria.backend.repository;

import com.bspapeleria.backend.entity.OrdenDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrdenDetalleRepository extends JpaRepository<OrdenDetalle, Long> {

    List<OrdenDetalle> findByOrdenId(Long ordenId);
}