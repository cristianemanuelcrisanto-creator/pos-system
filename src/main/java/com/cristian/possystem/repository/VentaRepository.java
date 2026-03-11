package com.cristian.possystem.repository;

import com.cristian.possystem.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v")
    BigDecimal obtenerTotalVentas();

    @Query(value = """
            SELECT COALESCE(SUM(total), 0)
            FROM ventas
            WHERE fecha_hora::date = CURRENT_DATE
            """, nativeQuery = true)
    BigDecimal obtenerTotalVentasDelDia();

    @Query(value = """
            SELECT COALESCE(SUM(total), 0)
            FROM ventas
            WHERE EXTRACT(YEAR FROM fecha_hora) = EXTRACT(YEAR FROM CURRENT_DATE)
              AND EXTRACT(MONTH FROM fecha_hora) = EXTRACT(MONTH FROM CURRENT_DATE)
            """, nativeQuery = true)
    BigDecimal obtenerTotalVentasDelMes();
}