package com.batoilogic.api.controller;

import com.batoilogic.api.entity.*;
import com.batoilogic.api.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/pedidosprov")
public class PedidoProvController {

    private final PedidoProvRepository pedidoProvRepo;
    private final ProductoRepository productoRepo;
    private final ProveedorRepository proveedorRepo;
    private final InventarioRepository inventarioRepo;
    private final TarifaProveedorRepository tarifaRepo;

    public PedidoProvController(PedidoProvRepository pedidoProvRepo,
                                 ProductoRepository productoRepo,
                                 ProveedorRepository proveedorRepo,
                                 InventarioRepository inventarioRepo,
                                 TarifaProveedorRepository tarifaRepo) {
        this.pedidoProvRepo = pedidoProvRepo;
        this.productoRepo = productoRepo;
        this.proveedorRepo = proveedorRepo;
        this.inventarioRepo = inventarioRepo;
        this.tarifaRepo = tarifaRepo;
    }

    // GET /pedidosprov
    @GetMapping
    public ResponseEntity<?> getPedidosProv(@RequestParam(required = false) String estado,
                                             HttpServletRequest request) {
        String rol = (String) request.getAttribute("rol");
        if (!"admin".equals(rol)) {
            return ResponseEntity.status(403).body(Map.of("error", "Solo admin puede ver pedidos a proveedor"));
        }

        List<PedidoProv> pedidos = estado != null
                ? pedidoProvRepo.findByEstado(estado)
                : pedidoProvRepo.findAll();

        return ResponseEntity.ok(pedidos);
    }

    // POST /pedidosprov — crear pedido a proveedor manualmente
    @PostMapping
    public ResponseEntity<?> crearPedidoProv(@RequestParam Long productoId,
                                              @RequestParam Long proveedorId,
                                              @RequestParam Integer cantidad,
                                              HttpServletRequest request) {
        String rol = (String) request.getAttribute("rol");
        if (!"admin".equals(rol)) {
            return ResponseEntity.status(403).body(Map.of("error", "Solo admin puede crear pedidos a proveedor"));
        }

        Optional<Producto> prod = productoRepo.findById(productoId);
        if (prod.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Producto no encontrado"));

        Optional<Proveedor> prov = proveedorRepo.findById(proveedorId);
        if (prov.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Proveedor no encontrado"));

        // Buscar precio de la tarifa
        TarifaProveedorId tarifaId = new TarifaProveedorId();
        tarifaId.setProductoId(productoId);
        tarifaId.setProveedorId(proveedorId);
        Optional<TarifaProveedor> tarifa = tarifaRepo.findById(tarifaId);
        BigDecimal precio = tarifa.map(TarifaProveedor::getPrecio).orElse(BigDecimal.ZERO);

        PedidoProv pp = new PedidoProv();
        pp.setProducto(prod.get());
        pp.setProveedor(prov.get());
        pp.setCantidad(cantidad);
        pp.setPrecioCompra(precio);
        pp.setFecha(LocalDate.now());
        pp.setEstado("Pendiente");
        pedidoProvRepo.save(pp);

        return ResponseEntity.status(201).body(pp);
    }

    // PATCH /pedidosprov/{id}/estado
    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> updateEstado(@PathVariable Long id,
                                          @RequestParam String estado,
                                          HttpServletRequest request) {
        String rol = (String) request.getAttribute("rol");
        if (!"admin".equals(rol)) {
            return ResponseEntity.status(403).body(Map.of("error", "Solo admin puede actualizar pedidos a proveedor"));
        }

        List<String> estadosValidos = List.of("Pendiente", "Recibido", "Cancelado");
        if (!estadosValidos.contains(estado)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Estado inválido", "valores", estadosValidos));
        }

        Optional<PedidoProv> opt = pedidoProvRepo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Pedido no encontrado"));

        PedidoProv pp = opt.get();
        pp.setEstado(estado);

        // Si se recibe el pedido, actualizar inventario
        if ("Recibido".equals(estado)) {
            inventarioRepo.findByProductoId(pp.getProducto().getId()).ifPresent(inv -> {
                inv.setStock(inv.getStock() + pp.getCantidad());
                inventarioRepo.save(inv);
            });
        }

        pedidoProvRepo.save(pp);
        return ResponseEntity.ok(pp);
    }

    // POST /pedidosprov/auto — generar pedidos automáticos por stock bajo
    @PostMapping("/auto")
    public ResponseEntity<?> generarPedidosAuto(HttpServletRequest request) {
        String rol = (String) request.getAttribute("rol");
        if (!"admin".equals(rol)) {
            return ResponseEntity.status(403).body(Map.of("error", "Solo admin puede generar pedidos automáticos"));
        }

        List<Inventario> inventarios = inventarioRepo.findAll();
        int generados = 0;

        for (Inventario inv : inventarios) {
            if (inv.getStock() <= inv.getStockMin()) {
                // Buscar proveedor con mejor precio para este producto
                TarifaProveedorId tarifaId = new TarifaProveedorId();
                List<TarifaProveedor> tarifas = tarifaRepo.findAll().stream()
                        .filter(t -> t.getId().getProductoId().equals(inv.getProducto().getId()))
                        .sorted((a, b) -> a.getPrecio().compareTo(b.getPrecio()))
                        .toList();

                if (!tarifas.isEmpty()) {
                    TarifaProveedor mejorTarifa = tarifas.get(0);
                    int cantidadPedir = inv.getStockMin() * 2; // pedir el doble del mínimo

                    PedidoProv pp = new PedidoProv();
                    pp.setProducto(inv.getProducto());
                    pp.setProveedor(mejorTarifa.getProveedor());
                    pp.setCantidad(cantidadPedir);
                    pp.setPrecioCompra(mejorTarifa.getPrecio());
                    pp.setFecha(LocalDate.now());
                    pp.setEstado("Pendiente");
                    pedidoProvRepo.save(pp);
                    generados++;
                }
            }
        }

        return ResponseEntity.ok(Map.of("mensaje", "Pedidos automáticos generados", "generados", generados));
    }
}
