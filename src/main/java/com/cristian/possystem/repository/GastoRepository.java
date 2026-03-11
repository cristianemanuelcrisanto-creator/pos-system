package com.cristian.possystem.repository;

import com.cristian.possystem.model.Gasto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface GastoRepository extends JpaRepository<Gasto, Long> {

    @Query("SELECT COALESCE(SUM(g.monto), 0) FROM Gasto g")
    BigDecimal obtenerTotalGastos();

    @Query(value = """
            SELECT COALESCE(SUM(monto), 0)
            FROM gastos
            WHERE fecha_hora::date = CURRENT_DATE
            """, nativeQuery = true)
    BigDecimal obtenerTotalGastosDelDia();

    @Query(value = """
            SELECT COALESCE(SUM(monto), 0)
            FROM gastos
            WHERE EXTRACT(YEAR FROM fecha_hora) = EXTRACT(YEAR FROM CURRENT_DATE)
              AND EXTRACT(MONTH FROM fecha_hora) = EXTRACT(MONTH FROM CURRENT_DATE)
            """, nativeQuery = true)
    BigDecimal obtenerTotalGastosDelMes();
}