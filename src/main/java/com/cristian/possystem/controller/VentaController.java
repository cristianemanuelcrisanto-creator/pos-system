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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

        cargarDatosFormulario(model, carrito);
        return "ventas/formulario";
    }

    @PostMapping("/agregar")
    public String agregarAlCarrito(@RequestParam Long productoId,
                                   @RequestParam Integer cantidad,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {

        Producto producto = productoRepository.findById(productoId).orElseThrow();

        if (cantidad <= 0) {
            redirectAttributes.addFlashAttribute("error", "La cantidad debe ser mayor a 0");
            return "redirect:/ventas/nueva";
        }

        List<ItemVenta> carrito = (List<ItemVenta>) session.getAttribute("carrito");
        if (carrito == null) {
            carrito = new ArrayList<>();
        }

        if (Boolean.TRUE.equals(producto.getControlaStock())) {
            int cantidadEnCarrito = carrito.stream()
                    .filter(item -> item.getProductoId().equals(productoId))
                    .mapToInt(ItemVenta::getCantidad)
                    .sum();

            if (cantidadEnCarrito + cantidad > producto.getStockActual()) {
                redirectAttributes.addFlashAttribute("error",
                        "No hay suficiente stock para: " + producto.getNombre());
                return "redirect:/ventas/nueva";
            }
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

    @PostMapping("/agregar-extra")
    public String agregarExtra(@RequestParam String nombreExtra,
                               @RequestParam BigDecimal precioExtra,
                               @RequestParam(defaultValue = "1") Integer cantidad,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {

        if (nombreExtra == null || nombreExtra.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Debes escribir el nombre del extra");
            return "redirect:/ventas/nueva";
        }

        if (precioExtra == null || precioExtra.compareTo(BigDecimal.ZERO) <= 0) {
            redirectAttributes.addFlashAttribute("error", "El precio del extra debe ser mayor a 0");
            return "redirect:/ventas/nueva";
        }

        if (cantidad <= 0) {
            redirectAttributes.addFlashAttribute("error", "La cantidad del extra debe ser mayor a 0");
            return "redirect:/ventas/nueva";
        }

        Producto productoExtra = productoRepository
                .findFirstByNombreIgnoreCase("Extra restaurante")
                .orElse(null);

        if (productoExtra == null) {
            redirectAttributes.addFlashAttribute("error",
                    "No existe el producto base 'Extra restaurante'. Créalo primero.");
            return "redirect:/ventas/nueva";
        }

        List<ItemVenta> carrito = (List<ItemVenta>) session.getAttribute("carrito");
        if (carrito == null) {
            carrito = new ArrayList<>();
        }

        BigDecimal subtotal = precioExtra.multiply(BigDecimal.valueOf(cantidad));

        carrito.add(new ItemVenta(
                productoExtra.getId(),
                nombreExtra.trim(),
                cantidad,
                precioExtra,
                subtotal
        ));

        session.setAttribute("carrito", carrito);
        redirectAttributes.addFlashAttribute("success", "Extra agregado correctamente");

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
                               @RequestParam BigDecimal montoRecibido,
                               HttpSession session,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        List<ItemVenta> carrito = (List<ItemVenta>) session.getAttribute("carrito");

        if (carrito == null || carrito.isEmpty()) {
            model.addAttribute("error", "El carrito está vacío");
            cargarDatosFormulario(model, new ArrayList<>());
            return "ventas/formulario";
        }

        BigDecimal total = carrito.stream()
                .map(ItemVenta::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        for (ItemVenta item : carrito) {
            Producto producto = productoRepository.findById(item.getProductoId()).orElseThrow();

            if (Boolean.TRUE.equals(producto.getControlaStock())
                    && producto.getStockActual() < item.getCantidad()) {

                model.addAttribute("error", "No hay suficiente stock para: " + producto.getNombre());
                cargarDatosFormulario(model, carrito);
                return "ventas/formulario";
            }
        }

        if (montoRecibido.compareTo(total) < 0) {
            model.addAttribute("error", "El monto recibido no puede ser menor al total");
            cargarDatosFormulario(model, carrito);
            return "ventas/formulario";
        }

        BigDecimal cambio = montoRecibido.subtract(total);

        Venta venta = new Venta();
        venta.setFechaHora(LocalDateTime.now());
        venta.setMetodoPago(metodoPago);
        venta.setTotal(total);
        venta.setMontoRecibido(montoRecibido);
        venta.setCambio(cambio);

        List<DetalleVenta> detalles = new ArrayList<>();

        for (ItemVenta item : carrito) {
            Producto producto = productoRepository.findById(item.getProductoId()).orElseThrow();

            if (Boolean.TRUE.equals(producto.getControlaStock())) {
                producto.setStockActual(producto.getStockActual() - item.getCantidad());
                productoRepository.save(producto);
            }

            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(item.getPrecioUnitario());
            detalle.setSubtotal(item.getSubtotal());

            detalles.add(detalle);
        }

        venta.setDetalles(detalles);
        ventaRepository.save(venta);

        session.removeAttribute("carrito");

        redirectAttributes.addFlashAttribute("success", "Venta registrada correctamente");
        return "redirect:/ventas/nueva";
    }

    private void cargarDatosFormulario(Model model, List<ItemVenta> carrito) {
        BigDecimal total = carrito.stream()
                .map(ItemVenta::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Producto comidaCompleta = productoRepository.findFirstByNombreIgnoreCase("Comida completa").orElse(null);
        Producto comidaSencilla = productoRepository.findFirstByNombreIgnoreCase("Comida sencilla").orElse(null);

        model.addAttribute("productos", productoRepository.findAll());
        model.addAttribute("carrito", carrito);
        model.addAttribute("total", total);
        model.addAttribute("comidaCompleta", comidaCompleta);
        model.addAttribute("comidaSencilla", comidaSencilla);
    }
}