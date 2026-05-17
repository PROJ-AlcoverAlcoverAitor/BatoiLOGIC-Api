package com.batoilogic.api.controller;

import com.batoilogic.api.entity.*;
import com.batoilogic.api.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteRepository clienteRepo;
    private final DireccionRepository direccionRepo;
    private final CodigoPostalRepository cpRepo;
    private final PedidoRepository pedidoRepo;

    public ClienteController(ClienteRepository clienteRepo,
                             DireccionRepository direccionRepo,
                             CodigoPostalRepository cpRepo,
                             PedidoRepository pedidoRepo) {
        this.clienteRepo = clienteRepo;
        this.direccionRepo = direccionRepo;
        this.cpRepo = cpRepo;
        this.pedidoRepo = pedidoRepo;
    }

    // GET /clientes/{id}/perfil
    @GetMapping("/{id}/perfil")
    public ResponseEntity<?> getPerfil(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String rol = (String) request.getAttribute("rol");

        if (!"admin".equals(rol) && !userId.equals(id)) {
            return ResponseEntity.status(403).body(Map.of("error", "No autorizado"));
        }

        Optional<Cliente> opt = clienteRepo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Cliente no encontrado"));

        Cliente c = opt.get();
        return ResponseEntity.ok(Map.of(
                "id", c.getId(),
                "nombre", c.getNombre(),
                "email", c.getEmail(),
                "telefono", c.getTelefono() != null ? c.getTelefono() : ""
        ));
    }

    // PUT /clientes/{id}/perfil
    @PutMapping("/{id}/perfil")
    public ResponseEntity<?> updatePerfil(@PathVariable Long id,
                                          @RequestParam(required = false) String nombre,
                                          @RequestParam(required = false) String telefono,
                                          HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String rol = (String) request.getAttribute("rol");

        if (!"admin".equals(rol) && !userId.equals(id)) {
            return ResponseEntity.status(403).body(Map.of("error", "No autorizado"));
        }

        Optional<Cliente> opt = clienteRepo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Cliente no encontrado"));

        Cliente c = opt.get();
        if (nombre != null) c.setNombre(nombre);
        if (telefono != null) c.setTelefono(telefono);
        clienteRepo.save(c);

        return ResponseEntity.ok(Map.of("mensaje", "Perfil actualizado"));
    }

    // GET /clientes/{id}/direcciones
    @GetMapping("/{id}/direcciones")
    public ResponseEntity<?> getDirecciones(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String rol = (String) request.getAttribute("rol");

        if (!"admin".equals(rol) && !userId.equals(id)) {
            return ResponseEntity.status(403).body(Map.of("error", "No autorizado"));
        }

        return ResponseEntity.ok(direccionRepo.findByClienteId(id));
    }

    // POST /clientes/{id}/direcciones
    @PostMapping("/{id}/direcciones")
    public ResponseEntity<?> addDireccion(@PathVariable Long id,
                                          @RequestParam String calle,
                                          @RequestParam(required = false) String numero,
                                          @RequestParam(required = false) String piso,
                                          @RequestParam String codigoPostal,
                                          @RequestParam(defaultValue = "false") Boolean esPrincipal,
                                          HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String rol = (String) request.getAttribute("rol");

        if (!"admin".equals(rol) && !userId.equals(id)) {
            return ResponseEntity.status(403).body(Map.of("error", "No autorizado"));
        }

        Optional<Cliente> cliente = clienteRepo.findById(id);
        if (cliente.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Cliente no encontrado"));

        Optional<CodigoPostal> cp = cpRepo.findByCodigo(codigoPostal);
        if (cp.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Código postal no encontrado"));

        Direccion d = new Direccion();
        d.setCliente(cliente.get());
        d.setCodigoPostal(cp.get());
        d.setCalle(calle);
        d.setNumero(numero);
        d.setPiso(piso);
        d.setEsPrincipal(esPrincipal);
        direccionRepo.save(d);

        return ResponseEntity.status(201).body(Map.of("mensaje", "Dirección añadida", "id", d.getId()));
    }

    // DELETE /clientes/{id}/direcciones/{dirId}
    @DeleteMapping("/{id}/direcciones/{dirId}")
    public ResponseEntity<?> deleteDireccion(@PathVariable Long id,
                                              @PathVariable Long dirId,
                                              HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String rol = (String) request.getAttribute("rol");

        if (!"admin".equals(rol) && !userId.equals(id)) {
            return ResponseEntity.status(403).body(Map.of("error", "No autorizado"));
        }

        Optional<Direccion> opt = direccionRepo.findById(dirId);
        if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Dirección no encontrada"));
        if (!opt.get().getCliente().getId().equals(id)) {
            return ResponseEntity.status(403).body(Map.of("error", "No autorizado"));
        }

        direccionRepo.deleteById(dirId);
        return ResponseEntity.ok(Map.of("mensaje", "Dirección eliminada"));
    }

    // GET /clientes/{id}/pedidos (historial)
    @GetMapping("/{id}/pedidos")
    public ResponseEntity<?> getHistorialPedidos(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String rol = (String) request.getAttribute("rol");

        if (!"admin".equals(rol) && !userId.equals(id)) {
            return ResponseEntity.status(403).body(Map.of("error", "No autorizado"));
        }

        List<?> pedidos = pedidoRepo.findByClienteId(id);
        return ResponseEntity.ok(Map.of("pedidos", pedidos));
    }
}
