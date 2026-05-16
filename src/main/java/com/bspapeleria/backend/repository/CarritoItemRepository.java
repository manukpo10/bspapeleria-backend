package com.bspapeleria.backend.repository;

import com.bspapeleria.backend.entity.CarritoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CarritoItemRepository extends JpaRepository<CarritoItem, Long> {

    List<CarritoItem> findByCarritoId(Long carritoId);

    void deleteByCarritoIdAndTipoItemAndItemId(Long carritoId, String tipoItem, String itemId);

    void deleteByCarritoId(Long carritoId);
}