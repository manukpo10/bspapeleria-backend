package com.bspapeleria.backend.repository;

import com.bspapeleria.backend.entity.Curso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CursoRepository extends JpaRepository<Curso, Long> {

    @Query("SELECT DISTINCT c FROM Curso c LEFT JOIN FETCH c.tags LEFT JOIN FETCH c.lecciones WHERE c.slug = :slug AND c.activo = true")
    Optional<Curso> findBySlug(@Param("slug") String slug);

    @Query("SELECT DISTINCT c FROM Curso c LEFT JOIN FETCH c.tags WHERE c.activo = true")
    Page<Curso> findByActivoTrue(Pageable pageable);

    @Query("SELECT DISTINCT c FROM Curso c LEFT JOIN FETCH c.tags WHERE c.nivel = :nivel AND c.activo = true")
    Page<Curso> findByNivelAndActivoTrue(@Param("nivel") Curso.Nivel nivel, Pageable pageable);

    @Query("SELECT DISTINCT c FROM Curso c LEFT JOIN FETCH c.tags WHERE c.modalidad = :modalidad AND c.activo = true")
    Page<Curso> findByModalidadAndActivoTrue(@Param("modalidad") Curso.Modalidad modalidad, Pageable pageable);

    @Query("SELECT DISTINCT c FROM Curso c LEFT JOIN FETCH c.tags WHERE c.activo = true AND " +
           "(LOWER(c.titulo) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.descripcion) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.instructor) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Curso> searchCursos(@Param("search") String search, Pageable pageable);

    @Query("SELECT DISTINCT c FROM Curso c LEFT JOIN FETCH c.tags WHERE c.activo = true AND c.precio >= :minPrice AND c.precio <= :maxPrice")
    Page<Curso> findByPrecioRange(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice, Pageable pageable);

    boolean existsBySlug(String slug);
}