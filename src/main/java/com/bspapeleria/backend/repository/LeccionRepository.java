package com.bspapeleria.backend.repository;

import com.bspapeleria.backend.entity.Leccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LeccionRepository extends JpaRepository<Leccion, Long> {

    List<Leccion> findByModuloIdOrderByOrdenAsc(Long moduloId);

    int countByModuloId(Long moduloId);
}