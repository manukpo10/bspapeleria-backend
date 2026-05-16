package com.bspapeleria.backend.repository;

import com.bspapeleria.backend.entity.Valoracion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ValoracionRepository extends JpaRepository<Valoracion, Long> {

    List<Valoracion> findByEntidadTipoAndEntidadIdOrderByFechaCreacionDesc(String entidadTipo, Long entidadId);

    Page<Valoracion> findByEntidadTipoAndEntidadId(String entidadTipo, Long entidadId, Pageable pageable);

    boolean existsByUsuarioIdAndEntidadTipoAndEntidadId(Long usuarioId, String entidadTipo, Long entidadId);

    @Query("SELECT AVG(v.calificacion) FROM Valoracion v WHERE v.entidadTipo = :tipo AND v.entidadId = :id")
    Double getPromedioCalificacion(@Param("tipo") String entidadTipo, @Param("id") Long entidadId);

    @Query("SELECT COUNT(v) FROM Valoracion v WHERE v.entidadTipo = :tipo AND v.entidadId = :id")
    int countByEntidad(@Param("tipo") String entidadTipo, @Param("id") Long entidadId);
}