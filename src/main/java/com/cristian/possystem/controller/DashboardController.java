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

        BigDecimal ventasTotales = ventaRepository.obtenerTotalVentas();
        BigDecimal gastosTotales = gastoRepository.obtenerTotalGastos();
        BigDecimal utilidadTotal = ventasTotales.subtract(gastosTotales);

        BigDecimal ventasHoy = ventaRepository.obtenerTotalVentasDelDia();
        BigDecimal gastosHoy = gastoRepository.obtenerTotalGastosDelDia();
        BigDecimal utilidadHoy = ventasHoy.subtract(gastosHoy);

        BigDecimal ventasMes = ventaRepository.obtenerTotalVentasDelMes();
        BigDecimal gastosMes = gastoRepository.obtenerTotalGastosDelMes();
        BigDecimal utilidadMes = ventasMes.subtract(gastosMes);

        long totalProductos = productoRepository.count();
        long productosStockBajo = productoRepository.findByStockActualLessThanEqual(5).size();

        model.addAttribute("ventasTotales", ventasTotales);
        model.addAttribute("gastosTotales", gastosTotales);
        model.addAttribute("utilidadTotal", utilidadTotal);

        model.addAttribute("ventasHoy", ventasHoy);
        model.addAttribute("gastosHoy", gastosHoy);
        model.addAttribute("utilidadHoy", utilidadHoy);

        model.addAttribute("ventasMes", ventasMes);
        model.addAttribute("gastosMes", gastosMes);
        model.addAttribute("utilidadMes", utilidadMes);

        model.addAttribute("totalProductos", totalProductos);
        model.addAttribute("productosStockBajo", productosStockBajo);

        return "dashboard";
    }
}