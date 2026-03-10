package com.cristian.possystem.controller;

import com.cristian.possystem.model.Gasto;
import com.cristian.possystem.repository.GastoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/gastos")
public class GastoController {

    private final GastoRepository gastoRepository;

    public GastoController(GastoRepository gastoRepository) {
        this.gastoRepository = gastoRepository;
    }

    @GetMapping
    public String listarGastos(Model model) {
        model.addAttribute("gastos", gastoRepository.findAll());
        return "gastos/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("gasto", new Gasto());
        return "gastos/formulario";
    }

    @PostMapping("/guardar")
    public String guardarGasto(@ModelAttribute Gasto gasto) {
        gasto.setFechaHora(LocalDateTime.now());
        gastoRepository.save(gasto);
        return "redirect:/gastos";
    }
}