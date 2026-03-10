package com.cristian.possystem.controller;

import com.cristian.possystem.model.CorteCaja;
import com.cristian.possystem.repository.CorteCajaRepository;
import com.cristian.possystem.repository.VentaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/cortes")
public class CorteCajaController {

    private final CorteCajaRepository corteCajaRepository;
    private final VentaRepository ventaRepository;

    public CorteCajaController(CorteCajaRepository corteCajaRepository, VentaRepository ventaRepository) {
        this.corteCajaRepository = corteCajaRepository;
        this.ventaRepository = ventaRepository;
    }

    @GetMapping
    public String listarCortes(Model model) {
        model.addAttribute("cortes", corteCajaRepository.findAll());
        return "cortes/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        BigDecimal ventasDelDia = ventaRepository.obtenerTotalVentasDelDia();

        CorteCaja corteCaja = new CorteCaja();
        corteCaja.setFecha(LocalDate.now());
        corteCaja.setVentasSistema(ventasDelDia);

        model.addAttribute("corteCaja", corteCaja);
        return "cortes/formulario";
    }

    @PostMapping("/guardar")
    public String guardarCorte(@ModelAttribute CorteCaja corteCaja) {
        BigDecimal diferencia = corteCaja.getEfectivoReal().subtract(corteCaja.getVentasSistema());
        corteCaja.setDiferencia(diferencia);
        corteCajaRepository.save(corteCaja);
        return "redirect:/cortes";
    }
}