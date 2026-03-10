package com.cristian.possystem.controller;

import com.cristian.possystem.dto.ItemVenta;
import com.cristian.possystem.model.DetalleVenta;
import com.cristian.possystem.model.Producto;
import com.cristian.possystem.model.Venta;
import com.cristian.possystem.repository.ProductoRepository;
import com.cristian.possystem.repository.VentaRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/ventas")
public class VentaController {

    private final ProductoRepository productoRepository;
    private final VentaRepository ventaRepository;

    public VentaController(ProductoRepository productoRepository, VentaRepository ventaRepository) {
        this.productoRepository = productoRepository;
        this.ventaRepository = ventaRepository;
    }

    @GetMapping
    public String listarVentas(Model model) {
        model.addAttribute("ventas", ventaRepository.findAll());
        return "ventas/lista";
    }

    @GetMapping("/nueva")
    public String mostrarFormularioVenta(Model model, HttpSession session) {
        List<ItemVenta> carrito = (List<ItemVenta>) session.getAttribute("carrito");
        if (carrito == null) {
            carrito = new ArrayList<>();
            session.setAttribute("carrito", carrito);
        }

        BigDecimal total = carrito.stream()
                .map(ItemVenta::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("productos", productoRepository.findAll());
        model.addAttribute("carrito", carrito);
        model.addAttribute("total", total);

        return "ventas/formulario";
    }

    @PostMapping("/agregar")
    public String agregarAlCarrito(@RequestParam Long productoId,
                                   @RequestParam Integer cantidad,
                                   HttpSession session,
                                   Model model) {

        Producto producto = productoRepository.findById(productoId).orElseThrow();

        if (cantidad <= 0) {
            return "redirect:/ventas/nueva";
        }

        List<ItemVenta> carrito = (List<ItemVenta>) session.getAttribute("carrito");
        if (carrito == null) {
            carrito = new ArrayList<>();
        }

        BigDecimal subtotal = producto.getPrecioVenta().multiply(BigDecimal.valueOf(cantidad));

        boolean encontrado = false;

        for (ItemVenta item : carrito) {
            if (item.getProductoId().equals(productoId)) {
                int nuevaCantidad = item.getCantidad() + cantidad;
                item.setCantidad(nuevaCantidad);
                item.setSubtotal(item.getPrecioUnitario().multiply(BigDecimal.valueOf(nuevaCantidad)));
                encontrado = true;
                break;
            }
        }

        if (!encontrado) {
            carrito.add(new ItemVenta(
                    producto.getId(),
                    producto.getNombre(),
                    cantidad,
                    producto.getPrecioVenta(),
                    subtotal
            ));
        }

        session.setAttribute("carrito", carrito);

        return "redirect:/ventas/nueva";
    }

    @GetMapping("/eliminar-item/{productoId}")
    public String eliminarItem(@PathVariable Long productoId, HttpSession session) {
        List<ItemVenta> carrito = (List<ItemVenta>) session.getAttribute("carrito");
        if (carrito != null) {
            carrito.removeIf(item -> item.getProductoId().equals(productoId));
            session.setAttribute("carrito", carrito);
        }
        return "redirect:/ventas/nueva";
    }

    @PostMapping("/guardar")
    public String guardarVenta(@RequestParam String metodoPago,
                               HttpSession session,
                               Model model) {

        List<ItemVenta> carrito = (List<ItemVenta>) session.getAttribute("carrito");

        if (carrito == null || carrito.isEmpty()) {
            model.addAttribute("error", "El carrito está vacío");
            model.addAttribute("productos", productoRepository.findAll());
            model.addAttribute("carrito", new ArrayList<>());
            model.addAttribute("total", BigDecimal.ZERO);
            return "ventas/formulario";
        }

        for (ItemVenta item : carrito) {
            Producto producto = productoRepository.findById(item.getProductoId()).orElseThrow();
            if (producto.getStockActual() < item.getCantidad()) {
                model.addAttribute("error", "No hay suficiente stock para: " + producto.getNombre());
                model.addAttribute("productos", productoRepository.findAll());
                model.addAttribute("carrito", carrito);

                BigDecimal total = carrito.stream()
                        .map(ItemVenta::getSubtotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                model.addAttribute("total", total);
                return "ventas/formulario";
            }
        }

        Venta venta = new Venta();
        venta.setFechaHora(LocalDateTime.now());
        venta.setMetodoPago(metodoPago);

        BigDecimal total = BigDecimal.ZERO;
        List<DetalleVenta> detalles = new ArrayList<>();

        for (ItemVenta item : carrito) {
            Producto producto = productoRepository.findById(item.getProductoId()).orElseThrow();

            producto.setStockActual(producto.getStockActual() - item.getCantidad());
            productoRepository.save(producto);

            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(item.getPrecioUnitario());
            detalle.setSubtotal(item.getSubtotal());

            detalles.add(detalle);
            total = total.add(item.getSubtotal());
        }

        venta.setTotal(total);
        venta.setDetalles(detalles);

        ventaRepository.save(venta);

        session.removeAttribute("carrito");

        return "redirect:/ventas";
    }
}