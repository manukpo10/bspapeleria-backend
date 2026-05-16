package com.bspapeleria.backend.repository;

import com.bspapeleria.backend.entity.Progreso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProgresoRepository extends JpaRepository<Progreso, Long> {

    Optional<Progreso> findByUsuarioIdAndCursoId(Long usuarioId, Long cursoId);

    List<Progreso> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioIdAndCursoId(Long usuarioId, Long cursoId);
}