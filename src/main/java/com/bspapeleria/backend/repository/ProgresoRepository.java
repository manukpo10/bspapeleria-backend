package com.bspapeleria.backend.repository;

import com.bspapeleria.backend.entity.Progreso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProgresoRepository extends JpaRepository<Progreso, Long> {

    Optional<Progreso> findByUsuarioIdAndCursoId(Long usuarioId, Long cursoId);

    @Query("SELECT p FROM Progreso p LEFT JOIN FETCH p.leccionesCompletadas LEFT JOIN FETCH p.curso WHERE p.usuario.id = :usuarioId")
    List<Progreso> findByUsuarioIdWithLecciones(Long usuarioId);

    @Query("SELECT p FROM Progreso p LEFT JOIN FETCH p.leccionesCompletadas LEFT JOIN FETCH p.curso WHERE p.usuario.id = :usuarioId AND p.curso.id = :cursoId")
    Optional<Progreso> findByUsuarioIdAndCursoIdWithLecciones(Long usuarioId, Long cursoId);

    List<Progreso> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioIdAndCursoId(Long usuarioId, Long cursoId);
}