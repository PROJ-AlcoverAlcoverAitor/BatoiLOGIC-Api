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
public class AlbaranFacturaController {

    private final PedidoRepository pedidoRepo;
    private final AlbaranRepository albaranRepo;
    private final FacturaRepository facturaRepo;
    private final LinPedidoRepository linPedidoRepo;

    public AlbaranFacturaController(PedidoRepository pedidoRepo,
                                    AlbaranRepository albaranRepo,
                                    FacturaRepository facturaRepo,
                                    LinPedidoRepository linPedidoRepo) {
        this.pedidoRepo = pedidoRepo;
        this.albaranRepo = albaranRepo;
        this.facturaRepo = facturaRepo;
        this.linPedidoRepo = linPedidoRepo;
    }

    // POST /pedidos/{id}/albaran — generar albarán
    @PostMapping("/pedidos/{id}/albaran")
    public ResponseEntity<?> generarAlbaran(@PathVariable Long id,
                                             @RequestParam(required = false) String observaciones,
                                             HttpServletRequest request) {
        String rol = (String) request.getAttribute("rol");
        if (!"admin".equals(rol) && !"empleado".equals(rol)) {
            return ResponseEntity.status(403).body(Map.of("error", "No autorizado"));
        }

        Optional<Pedido> pedidoOpt = pedidoRepo.findById(id);
        if (pedidoOpt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Pedido no encontrado"));

        if (albaranRepo.findByPedidoId(id).isPresent()) {
            return ResponseEntity.status(409).body(Map.of("error", "El albarán ya existe para este pedido"));
        }

        Albaran albaran = new Albaran();
        albaran.setPedido(pedidoOpt.get());
        albaran.setFecha(LocalDate.now());
        albaran.setObservaciones(observaciones);
        albaranRepo.save(albaran);

        return ResponseEntity.status(201).body(albaran);
    }

    // GET /pedidos/{id}/albaran
    @GetMapping("/pedidos/{id}/albaran")
    public ResponseEntity<?> getAlbaran(@PathVariable Long id, HttpServletRequest request) {
        Optional<Albaran> opt = albaranRepo.findByPedidoId(id);
        if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Albarán no encontrado"));
        return ResponseEntity.ok(opt.get());
    }

    // POST /pedidos/{id}/factura — generar factura
    @PostMapping("/pedidos/{id}/factura")
    public ResponseEntity<?> generarFactura(@PathVariable Long id,
                                             HttpServletRequest request) {
        String rol = (String) request.getAttribute("rol");
        if (!"admin".equals(rol)) {
            return ResponseEntity.status(403).body(Map.of("error", "Solo admin puede generar facturas"));
        }

        Optional<Pedido> pedidoOpt = pedidoRepo.findById(id);
        if (pedidoOpt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Pedido no encontrado"));

        if (facturaRepo.findByPedidoId(id).isPresent()) {
            return ResponseEntity.status(409).body(Map.of("error", "La factura ya existe para este pedido"));
        }

        // Calcular total desde líneas de pedido
        List<LinPedido> lineas = linPedidoRepo.findByPedidoId(id);
        BigDecimal total = lineas.stream()
                .map(l -> l.getPrecioUnitario().multiply(BigDecimal.valueOf(l.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Factura factura = new Factura();
        factura.setPedido(pedidoOpt.get());
        factura.setFecha(LocalDate.now());
        factura.setTotal(total);
        factura.setPagada(false);
        facturaRepo.save(factura);

        return ResponseEntity.status(201).body(factura);
    }

    // GET /pedidos/{id}/factura
    @GetMapping("/pedidos/{id}/factura")
    public ResponseEntity<?> getFactura(@PathVariable Long id, HttpServletRequest request) {
        Optional<Factura> opt = facturaRepo.findByPedidoId(id);
        if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Factura no encontrada"));
        return ResponseEntity.ok(opt.get());
    }

    // PATCH /facturas/{id}/pagar
    @PatchMapping("/facturas/{id}/pagar")
    public ResponseEntity<?> pagarFactura(@PathVariable Long id, HttpServletRequest request) {
        String rol = (String) request.getAttribute("rol");
        if (!"admin".equals(rol)) {
            return ResponseEntity.status(403).body(Map.of("error", "Solo admin puede marcar facturas como pagadas"));
        }

        Optional<Factura> opt = facturaRepo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Factura no encontrada"));

        Factura factura = opt.get();
        factura.setPagada(true);
        facturaRepo.save(factura);

        return ResponseEntity.ok(factura);
    }
}
