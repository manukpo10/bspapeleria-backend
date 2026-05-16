package com.bspapeleria.backend.repository;

import com.bspapeleria.backend.entity.Producto;
import com.bspapeleria.backend.entity.Producto.Categoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findBySlug(String slug);

    boolean existsBySlug(String slug);

    @Query("SELECT DISTINCT p FROM Producto p LEFT JOIN FETCH p.imagenes LEFT JOIN FETCH p.tags WHERE p.activo = true")
    Page<Producto> findByActivoTrue(Pageable pageable);

    @Query("SELECT DISTINCT p FROM Producto p LEFT JOIN FETCH p.imagenes LEFT JOIN FETCH p.tags WHERE p.activo = true AND p.categoria = :categoria")
    Page<Producto> findByCategoriaAndActivoTrue(@Param("categoria") Categoria categoria, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Producto p LEFT JOIN FETCH p.imagenes LEFT JOIN FETCH p.tags WHERE p.destacado = true AND p.activo = true")
    Page<Producto> findByDestacadoTrueAndActivoTrue(Pageable pageable);

    @Query("SELECT p FROM Producto p WHERE p.activo = true AND " +
           "(LOWER(p.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Producto> searchByNombreODescripcion(@Param("search") String search, Pageable pageable);

    @Query("SELECT p FROM Producto p WHERE p.activo = true AND " +
           "(:search = '' OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:categorias IS NULL OR p.categoria IN :categorias) AND " +
           "(:minPrice IS NULL OR p.precio >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.precio <= :maxPrice)")
    Page<Producto> searchWithFilters(@Param("search") String search,
                                     @Param("categorias") List<Categoria> categorias,
                                     @Param("minPrice") Double minPrice,
                                     @Param("maxPrice") Double maxPrice,
                                     Pageable pageable);

    List<Producto> findByIdIn(List<Long> ids);
}