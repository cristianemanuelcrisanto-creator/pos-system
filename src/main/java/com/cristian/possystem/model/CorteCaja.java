package com.cristian.possystem.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cortes_caja")
public class CorteCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fecha;
    private BigDecimal ventasSistema;
    private BigDecimal efectivoReal;
    private BigDecimal diferencia;
    private String observaciones;

    public CorteCaja() {
    }

    public Long getId() {
        return id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getVentasSistema() {
        return ventasSistema;
    }

    public void setVentasSistema(BigDecimal ventasSistema) {
        this.ventasSistema = ventasSistema;
    }

    public BigDecimal getEfectivoReal() {
        return efectivoReal;
    }

    public void setEfectivoReal(BigDecimal efectivoReal) {
        this.efectivoReal = efectivoReal;
    }

    public BigDecimal getDiferencia() {
        return diferencia;
    }

    public void setDiferencia(BigDecimal diferencia) {
        this.diferencia = diferencia;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}