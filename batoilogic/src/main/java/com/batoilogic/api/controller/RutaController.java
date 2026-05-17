package com.batoilogic.api.controller;

import com.batoilogic.api.entity.*;
import com.batoilogic.api.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/rutas")
public class RutaController {

    private final RutaRepository rutaRepo;
    private final PedidoRepository pedidoRepo;
    private final RepartidorRepository repartidorRepo;

    public RutaController(RutaRepository rutaRepo,
                          PedidoRepository pedidoRepo,
                          RepartidorRepository repartidorRepo) {
        this.rutaRepo = rutaRepo;
        this.pedidoRepo = pedidoRepo;
        this.repartidorRepo = repartidorRepo;
    }

    // GET /rutas?dia=
    @GetMapping
    public ResponseEntity<?> getRutas(@RequestParam(required = false) String dia,
                                      HttpServletRequest request) {
        String rol = (String) request.getAttribute("rol");
        Long userId = (Long) request.getAttribute("userId");

        List<Ruta> rutas;
        if ("admin".equals(rol)) {
            rutas = dia != null ? rutaRepo.findByFecha(LocalDate.parse(dia)) : rutaRepo.findAll();
        } else if ("empleado".equals(rol)) {
            if (dia == null) return ResponseEntity.badRequest().body(Map.of("error", "Parámetro 'dia' obligatorio"));
            rutas = rutaRepo.findByFechaAndRepartidorId(LocalDate.parse(dia), userId);
        } else {
            return ResponseEntity.status(403).body(Map.of("error", "No autorizado"));
        }
        return ResponseEntity.ok(rutas);
    }

    // POST /rutas — crear ruta y asignar repartidor
    @PostMapping
    public ResponseEntity<?> crearRuta(@RequestParam Long repartidorId,
                                       @RequestParam String fecha,
                                       HttpServletRequest request) {
        String rol = (String) request.getAttribute("rol");
        if (!"admin".equals(rol)) {
            return ResponseEntity.status(403).body(Map.of("error", "Solo admin puede crear rutas"));
        }

        Optional<Repartidor> rep = repartidorRepo.findById(repartidorId);
        if (rep.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Repartidor no encontrado"));

        Ruta ruta = new Ruta();
        ruta.setRepartidor(rep.get());
        ruta.setFecha(LocalDate.parse(fecha));
        rutaRepo.save(ruta);

        return ResponseEntity.status(201).body(ruta);
    }

    // POST /rutas/{id}/pedidos — asignar pedido a ruta
    @PostMapping("/{id}/pedidos")
    public ResponseEntity<?> asignarPedido(@PathVariable Long id,
                                            @RequestParam Long pedidoId,
                                            HttpServletRequest request) {
        String rol = (String) request.getAttribute("rol");
        if (!"admin".equals(rol)) {
            return ResponseEntity.status(403).body(Map.of("error", "Solo admin puede asignar pedidos"));
        }

        Optional<Ruta> rutaOpt = rutaRepo.findById(id);
        if (rutaOpt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Ruta no encontrada"));

        Optional<Pedido> pedidoOpt = pedidoRepo.findById(pedidoId);
        if (pedidoOpt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Pedido no encontrado"));

        Ruta ruta = rutaOpt.get();
        ruta.getPedidos().add(pedidoOpt.get());

        // Actualizar repartidor_id del pedido y estado
        Pedido pedido = pedidoOpt.get();
        pedido.setRepartidorId(ruta.getRepartidor().getId());
        pedido.setEstado("PREPARAT");
        pedidoRepo.save(pedido);
        rutaRepo.save(ruta);

        return ResponseEntity.ok(Map.of("mensaje", "Pedido asignado a ruta", "rutaId", id, "pedidoId", pedidoId));
    }

    // DELETE /rutas/{id}/pedidos/{pedidoId} — quitar pedido de ruta
    @DeleteMapping("/{id}/pedidos/{pedidoId}")
    public ResponseEntity<?> quitarPedido(@PathVariable Long id,
                                           @PathVariable Long pedidoId,
                                           HttpServletRequest request) {
        String rol = (String) request.getAttribute("rol");
        if (!"admin".equals(rol)) {
            return ResponseEntity.status(403).body(Map.of("error", "Solo admin puede modificar rutas"));
        }

        Optional<Ruta> rutaOpt = rutaRepo.findById(id);
        if (rutaOpt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Ruta no encontrada"));

        Ruta ruta = rutaOpt.get();
        ruta.getPedidos().removeIf(p -> p.getId().equals(pedidoId));
        rutaRepo.save(ruta);

        return ResponseEntity.ok(Map.of("mensaje", "Pedido quitado de la ruta"));
    }

    // PATCH /rutas/{id}/estado
    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> updateEstado(@PathVariable Long id,
                                          @RequestParam String estado,
                                          HttpServletRequest request) {
        String rol = (String) request.getAttribute("rol");
        if (!"admin".equals(rol) && !"empleado".equals(rol)) {
            return ResponseEntity.status(403).body(Map.of("error", "No autorizado"));
        }

        Optional<Ruta> opt = rutaRepo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Ruta no encontrada"));

        Ruta ruta = opt.get();
        ruta.setEstado(estado);
        rutaRepo.save(ruta);

        return ResponseEntity.ok(ruta);
    }
}
