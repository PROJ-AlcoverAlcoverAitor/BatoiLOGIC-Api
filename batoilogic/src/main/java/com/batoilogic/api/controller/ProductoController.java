package com.batoilogic.api.controller;

import com.batoilogic.api.entity.Inventario;
import com.batoilogic.api.entity.Producto;
import com.batoilogic.api.repository.InventarioRepository;
import com.batoilogic.api.repository.ProductoRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoRepository productoRepo;
    private final InventarioRepository inventarioRepo;

    public ProductoController(ProductoRepository productoRepo,
                              InventarioRepository inventarioRepo) {
        this.productoRepo = productoRepo;
        this.inventarioRepo = inventarioRepo;
    }

    // GET /productos — listar todos
    @GetMapping
    public ResponseEntity<?> getProductos(
            @RequestParam(required = false) String nombre) {

        List<Producto> productos;
        if (nombre != null && !nombre.isEmpty()) {
            productos = productoRepo.findByNombreContainingIgnoreCase(nombre);
        } else {
            productos = productoRepo.findAll();
        }
        return ResponseEntity.ok(productos);
    }

    // GET /productos/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getProducto(@PathVariable Long id) {
        Optional<Producto> opt = productoRepo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Producto no encontrado"));
        return ResponseEntity.ok(opt.get());
    }

    // GET /productos/{id}/inventario
    @GetMapping("/{id}/inventario")
    public ResponseEntity<?> getInventario(@PathVariable Long id) {
        Optional<Inventario> opt = inventarioRepo.findByProductoId(id);
        if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Inventario no encontrado"));
        return ResponseEntity.ok(opt.get());
    }

    // PATCH /productos/{id}/inventario
    @PatchMapping("/{id}/inventario")
    public ResponseEntity<?> updateInventario(@PathVariable Long id,
                                               @RequestParam Integer stock,
                                               HttpServletRequest request) {
        String rol = (String) request.getAttribute("rol");
        if (!"admin".equals(rol)) {
            return ResponseEntity.status(403).body(Map.of("error", "Solo admin puede actualizar inventario"));
        }

        Optional<Inventario> opt = inventarioRepo.findByProductoId(id);
        if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Inventario no encontrado"));

        Inventario inv = opt.get();
        inv.setStock(stock);
        inv.setUpdatedAt(LocalDateTime.now());
        inventarioRepo.save(inv);

        return ResponseEntity.ok(inv);
    }

    // GET /productos/inventario — todos los productos con su stock
    @GetMapping("/inventario")
    public ResponseEntity<?> getInventarioCompleto(HttpServletRequest request) {
        String rol = (String) request.getAttribute("rol");
        if (!"admin".equals(rol))
            return ResponseEntity.status(403).body(Map.of("error", "Solo admins"));

        List<Inventario> inventario = inventarioRepo.findAll();
        List<Map<String, Object>> result = inventario.stream()
                .map(inv -> {
                    Map<String, Object> m = new java.util.HashMap<>();
                    m.put("id", inv.getId());
                    m.put("productoId", inv.getProducto().getId());
                    m.put("productoNombre", inv.getProducto().getNombre());
                    m.put("stock", inv.getStock());
                    m.put("stockMin", inv.getStockMin());
                    m.put("updatedAt", inv.getUpdatedAt());
                    return m;
                })
                .toList();

        return ResponseEntity.ok(result);
    }
}
