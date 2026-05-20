package com.batoilogic.api.controller;

import com.batoilogic.api.entity.Proveedor;
import com.batoilogic.api.repository.ProveedorRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/proveedores")
public class ProveedorController {

    private final ProveedorRepository proveedorRepo;

    public ProveedorController(ProveedorRepository proveedorRepo) {
        this.proveedorRepo = proveedorRepo;
    }

    // GET /proveedores
    @GetMapping
    public ResponseEntity<?> getProveedores(HttpServletRequest request) {
        String rol = (String) request.getAttribute("rol");
        if (!"admin".equals(rol))
            return ResponseEntity.status(403).body(Map.of("error", "Solo admins"));

        List<Map<String, Object>> result = proveedorRepo.findAll().stream()
                .map(p -> Map.<String, Object>of(
                        "id", p.getId(),
                        "nombre", p.getNombre()
                ))
                .toList();

        return ResponseEntity.ok(result);
    }
}