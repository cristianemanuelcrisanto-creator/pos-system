package com.cristian.possystem.controller;

import com.cristian.possystem.repository.GastoRepository;
import com.cristian.possystem.repository.ProductoRepository;
import com.cristian.possystem.repository.VentaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;

@Controller
public class DashboardController {

    private final VentaRepository ventaRepository;
    private final GastoRepository gastoRepository;
    private final ProductoRepository productoRepository;

    public DashboardController(VentaRepository ventaRepository,
                               GastoRepository gastoRepository,
                               ProductoRepository productoRepository) {
        this.ventaRepository = ventaRepository;
        this.gastoRepository = gastoRepository;
        this.productoRepository = productoRepository;
    }

    @GetMapping("/dashboard")
    public String mostrarDashboard(Model model) {
        BigDecimal totalVentas = ventaRepository.obtenerTotalVentas();
        BigDecimal totalGastos = gastoRepository.obtenerTotalGastos();
        BigDecimal utilidad = totalVentas.subtract(totalGastos);

        model.addAttribute("totalVentas", totalVentas);
        model.addAttribute("totalGastos", totalGastos);
        model.addAttribute("utilidad", utilidad);
        model.addAttribute("totalProductos", productoRepository.count());
        model.addAttribute("productosStockBajo", productoRepository.findByStockActualLessThanEqual(5));

        return "dashboard";
    }
}