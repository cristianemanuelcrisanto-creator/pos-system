package com.cristian.possystem.repository;

import com.cristian.possystem.model.Gasto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface GastoRepository extends JpaRepository<Gasto, Long> {

    @Query("SELECT COALESCE(SUM(g.monto), 0) FROM Gasto g")
    BigDecimal obtenerTotalGastos();
}