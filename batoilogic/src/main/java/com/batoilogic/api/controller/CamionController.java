package com.batoilogic.api.controller;

import com.batoilogic.api.entity.Camion;
import com.batoilogic.api.repository.CamionRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class CamionController {

    private final CamionRepository camionRepo;

    public CamionController(CamionRepository camionRepo) {
        this.camionRepo = camionRepo;
    }

    // GET /camiones
    @GetMapping("/camiones")
    public ResponseEntity<?> getCamiones(HttpServletRequest request) {
        String rol = (String) request.getAttribute("rol");
        if (!"admin".equals(rol))
            return ResponseEntity.status(403).body(Map.of("error", "Solo admins"));

        List<Map<String, Object>> result = camionRepo.findAll().stream()
                .map(c -> Map.<String, Object>of(
                        "id",        c.getId(),
                        "matricula", c.getMatricula(),
                        "modelo",    c.getModelo(),
                        "activo",    c.getActivo()
                ))
                .toList();

        return ResponseEntity.ok(result);
    }

    // POST /camiones
    @PostMapping("/camiones")
    public ResponseEntity<?> createCamion(@RequestBody Map<String, Object> body,
                                          HttpServletRequest request) {
        String rol = (String) request.getAttribute("rol");
        if (!"admin".equals(rol))
            return ResponseEntity.status(403).body(Map.of("error", "Solo admins"));

        Camion c = new Camion();
        c.setMatricula((String) body.get("matricula"));
        c.setModelo((String) body.get("modelo"));
        c.setActivo(body.get("activo") == null || (Boolean) body.get("activo"));
        camionRepo.save(c);

        return ResponseEntity.status(201).body(Map.of("id", c.getId(), "mensaje", "Camión creado"));
    }

    // PUT /camiones/{id}
    @PutMapping("/camiones/{id}")
    public ResponseEntity<?> updateCamion(@PathVariable Long id,
                                          @RequestBody Map<String, Object> body,
                                          HttpServletRequest request) {
        String rol = (String) request.getAttribute("rol");
        if (!"admin".equals(rol))
            return ResponseEntity.status(403).body(Map.of("error", "Solo admins"));

        Optional<Camion> opt = camionRepo.findById(id);
        if (opt.isEmpty())
            return ResponseEntity.status(404).body(Map.of("error", "Camión no encontrado"));

        Camion c = opt.get();
        if (body.containsKey("matricula")) c.setMatricula((String) body.get("matricula"));
        if (body.containsKey("modelo"))    c.setModelo((String) body.get("modelo"));
        if (body.containsKey("activo"))    c.setActivo((Boolean) body.get("activo"));
        camionRepo.save(c);

        return ResponseEntity.ok(Map.of("mensaje", "Camión actualizado"));
    }
}