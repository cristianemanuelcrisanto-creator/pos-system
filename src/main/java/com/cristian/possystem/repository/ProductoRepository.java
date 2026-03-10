package com.cristian.possystem.repository;

import com.cristian.possystem.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByStockActualLessThanEqual(Integer stockMinimo);
}