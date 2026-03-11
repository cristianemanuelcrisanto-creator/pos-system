package com.cristian.possystem.repository;

import com.cristian.possystem.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByStockActualLessThanEqual(Integer stockMinimo);

    Optional<Producto> findFirstByNombreIgnoreCase(String nombre);
}