package com.cristian.possystem.repository;

import com.cristian.possystem.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v")
    BigDecimal obtenerTotalVentas();
}