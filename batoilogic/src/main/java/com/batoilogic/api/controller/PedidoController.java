package com.batoilogic.api.controller;

import com.batoilogic.api.entity.Cliente;
import com.batoilogic.api.entity.Direccion;
import com.batoilogic.api.entity.Pedido;
import com.batoilogic.api.entity.Producto;
import com.batoilogic.api.repository.ClienteRepository;
import com.batoilogic.api.repository.DireccionRepository;
import com.batoilogic.api.repository.PedidoRepository;
import com.batoilogic.api.repository.ProductoRepository;

import com.batoilogic.api.service.NominatimService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoRepository pedidoRepo;
    private final NominatimService nominatimService;
    private final ProductoRepository productoRepo;
    private final ClienteRepository clienteRepo; // Inyección de ClienteRepository
    private final DireccionRepository direccionRepo;

    private static final List<String> ESTADOS_VALIDOS =
            List.of("EN PREPARACIÓ", "PREPARAT", "EN RUTA", "ENTREGAT", "NO ENTREGAT");

    // Constructor actualizado con todos los repositorios necesarios
    public PedidoController(PedidoRepository pedidoRepo,
                            NominatimService nominatimService,
                            ProductoRepository productoRepo,
                            ClienteRepository clienteRepo,
                            DireccionRepository direccionRepo) {
        this.pedidoRepo = pedidoRepo;
        this.nominatimService = nominatimService;
        this.productoRepo = productoRepo;
        this.clienteRepo = clienteRepo;
        this.direccionRepo = direccionRepo;
    }

    // GET /pedidos
    @GetMapping
    public ResponseEntity<?> getPedidos(@RequestParam(required = false) String dia,
                                        HttpServletRequest request) {
        String rol = (String) request.getAttribute("rol");
        Long userId = (Long) request.getAttribute("userId");

        List<Pedido> pedidos;

        if ("empleado".equals(rol)) {
            if (dia == null) return ResponseEntity.badRequest().body(Map.of("error", "Parámetro 'dia' es obligatorio"));
            pedidos = pedidoRepo.findByFechaAndRepartidorId(LocalDate.parse(dia), userId);
        } else if ("admin".equals(rol)) {
            pedidos = dia != null ? pedidoRepo.findByFecha(LocalDate.parse(dia)) : pedidoRepo.findAll();
        } else {
            return ResponseEntity.status(403).body(Map.of("error", "No autorizado"));
        }

        return ResponseEntity.ok(Map.of("day", dia, "comandas", pedidos));
    }

    // POST /pedidos — admin/empleado crea pedido con geocodificación
    @PostMapping
    public ResponseEntity<?> addPedido(@RequestParam String nombre,
                                       @RequestParam String direccion,
                                       @RequestParam String fecha,
                                       @RequestParam String telefono,
                                       @RequestParam Long repartidor_id,
                                       HttpServletRequest request) {
        String rol = (String) request.getAttribute("rol");
        if (!"admin".equals(rol) && !"empleado".equals(rol)) {
            return ResponseEntity.status(403).body(Map.of("error", "No autorizado"));
        }

        double[] coords = nominatimService.obtenerCoords(direccion);

        Pedido pedido = new Pedido();
        pedido.setCliente(nombre);
        pedido.setDireccion(direccion);
        pedido.setFecha(LocalDate.parse(fecha));
        pedido.setTelefono(telefono);
        pedido.setRepartidorId(repartidor_id);
        pedido.setEstado("EN PREPARACIÓ");
        pedido.setLat(coords[0]);
        pedido.setLng(coords[1]);

        pedidoRepo.save(pedido);
        return ResponseEntity.status(201).body(pedido);
    }

    // POST /pedidos/cliente — El cliente crea su propio pedido de forma segura usando su Token
    @PostMapping("/cliente")
    public ResponseEntity<?> crearPedidoCliente(@RequestParam Long clienteId,
                                                @RequestParam Long productoId,
                                                @RequestParam Integer cantidad,
                                                @RequestParam Long direccionId,
                                                HttpServletRequest request) {
        String rol = (String) request.getAttribute("rol");
        Long userId = (Long) request.getAttribute("userId");

        // Control de seguridad por Token JWT
        if (!"cliente".equals(rol) || !userId.equals(clienteId)) {
            return ResponseEntity.status(403).body(Map.of("error", "No autorizado"));
        }

        Optional<Producto> prodOpt = productoRepo.findById(productoId);
        if (prodOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Producto no encontrado"));
        }

        // Buscamos al cliente en la Base de Datos para sacar sus datos reales
        Optional<Cliente> cliOpt = clienteRepo.findById(userId);
        if (cliOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Cliente no encontrado"));
        }

        // Buscamos la dirección seleccionada por el cliente
        Optional<Direccion> dirOpt = direccionRepo.findById(direccionId);
        if (dirOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Dirección no encontrada"));
        }

        Direccion dirReal = dirOpt.get();

        // Verificamos que esa dirección pertenezca de verdad al cliente autenticado
        if (!dirReal.getCliente().getId().equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "La dirección no pertenece a este cliente"));
        }

        // Construimos la cadena de texto de la dirección para el registro y para Nominatim
        String numeroTexto = (dirReal.getNumero() != null && !dirReal.getNumero().trim().isEmpty())
                ? " " + dirReal.getNumero().trim()
                : "";
        String direccionTexto = dirReal.getCalle().trim() + numeroTexto + ", " + dirReal.getMunicipio().getNom().trim();

        // Geolocalizamos la dirección mediante el servicio externo de Nominatim
        double[] coords = nominatimService.obtenerCoords(direccionTexto);

        String nombreReal = cliOpt.get().getNombre();
        String telefonoReal = cliOpt.get().getTelefono();

        Pedido pedido = new Pedido();
        pedido.setClienteId(clienteId);
        pedido.setCliente(nombreReal);
        pedido.setDireccion(direccionTexto);
        pedido.setFecha(LocalDate.now());
        pedido.setEstado("EN PREPARACIÓ");

        if (telefonoReal != null && !telefonoReal.trim().isEmpty()) {
            String telLimpio = telefonoReal.trim();

            // Si el teléfono ya empieza por "+34" o "34", lo dejamos como está
            if (telLimpio.startsWith("+34")) {
                pedido.setTelefono(telLimpio);
            } else if (telLimpio.startsWith("34") && telLimpio.length() > 9) {
                pedido.setTelefono("+" + telLimpio);
            } else {
                // Si es un número normal de 9 dígitos, le añadimos el prefijo +34
                pedido.setTelefono("+34" + telLimpio);
            }
        } else {
            pedido.setTelefono(""); // Si no tiene teléfono en su perfil, se queda vacío
        }

        // Guardamos las coordenadas obtenidas del servicio de mapas
        pedido.setLat(coords[0]);
        pedido.setLng(coords[1]);

        pedidoRepo.save(pedido);

        return ResponseEntity.status(201).body(Map.of(
                "mensaje", "Pedido creado correctamente",
                "pedidoId", pedido.getId(),
                "estado", pedido.getEstado()
        ));
    }

    // GET /pedidos/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getPedido(@PathVariable Long id, HttpServletRequest request) {
        String rol = (String) request.getAttribute("rol");
        Long userId = (Long) request.getAttribute("userId");

        Optional<Pedido> opt = pedidoRepo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Pedido no encontrado"));

        Pedido pedido = opt.get();
        if ("empleado".equals(rol) && !pedido.getRepartidorId().equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "No autorizado"));
        }

        return ResponseEntity.ok(pedido);
    }

    // PATCH /pedidos/{id}/estado
    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> updateEstado(@PathVariable Long id,
                                          @RequestParam String estado,
                                          HttpServletRequest request) {
        String rol = (String) request.getAttribute("rol");
        Long userId = (Long) request.getAttribute("userId");

        if (!ESTADOS_VALIDOS.contains(estado)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Estado inválido", "valores", ESTADOS_VALIDOS));
        }

        Optional<Pedido> opt = pedidoRepo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Pedido no encontrado"));

        Pedido pedido = opt.get();
        if ("empleado".equals(rol) && !pedido.getRepartidorId().equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "No autorizado"));
        }

        pedido.setEstado(estado);
        pedidoRepo.save(pedido);
        return ResponseEntity.ok(pedido);
    }

    // PATCH /pedidos/{id}/incidencia
    @PatchMapping("/{id}/incidencia")
    public ResponseEntity<?> updateIncidencia(@PathVariable Long id,
                                              @RequestParam(required = false) String incidencia,
                                              HttpServletRequest request) {
        String rol = (String) request.getAttribute("rol");
        Long userId = (Long) request.getAttribute("userId");

        Optional<Pedido> opt = pedidoRepo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Pedido no encontrado"));

        Pedido pedido = opt.get();
        if ("empleado".equals(rol) && !pedido.getRepartidorId().equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "No autorizado"));
        }

        pedido.setIncidencia(incidencia);
        pedidoRepo.save(pedido);
        return ResponseEntity.ok(pedido);
    }
}