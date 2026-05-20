package com.batoilogic.api.controller;

import com.batoilogic.api.entity.Repartidor;
import com.batoilogic.api.repository.CamionRepository;
import com.batoilogic.api.repository.RepartidorRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class RepartidorController {

    private final RepartidorRepository repartidorRepo;
    private final CamionRepository camionRepo;

    public RepartidorController(RepartidorRepository repartidorRepo, CamionRepository camionRepo) {
        this.repartidorRepo = repartidorRepo;
        this.camionRepo = camionRepo;
    }

    // POST /repartidor/ubicacion
    @PostMapping("/repartidor/ubicacion")
    public ResponseEntity<?> updateUbicacion(@RequestParam Double lat,
                                              @RequestParam Double lng,
                                              HttpServletRequest request) {
        String rol = (String) request.getAttribute("rol");
        Long userId = (Long) request.getAttribute("userId");

        if (!"empleado".equals(rol)) {
            return ResponseEntity.status(403).body(Map.of("error", "Solo empleados pueden actualizar ubicación"));
        }

        Optional<Repartidor> opt = repartidorRepo.findById(userId);
        if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Empleado no encontrado"));

        Repartidor r = opt.get();
        r.setUbicacionLat(lat);
        r.setUbicacionLng(lng);
        r.setUbicacionActualizado(LocalDateTime.now());
        repartidorRepo.save(r);

        return ResponseEntity.ok(Map.of("mensaje", "Ubicación actualizada", "lat", lat, "lng", lng));
    }

    // GET /repartidores/ubicaciones
    @GetMapping("/repartidores/ubicaciones")
    public ResponseEntity<?> getUbicaciones(HttpServletRequest request) {
        String rol = (String) request.getAttribute("rol");

        if (!"admin".equals(rol)) {
            return ResponseEntity.status(403).body(Map.of("error", "Solo admins pueden ver la flota"));
        }

        List<Repartidor> repartidores = repartidorRepo.findAll();
        List<Map<String, Object>> ubicaciones = repartidores.stream()
                .filter(r -> r.getUbicacionLat() != null)
                .map(r -> Map.<String, Object>of(
                        "id", r.getId(),
                        "nombre", r.getNombre(),
                        "ubicacion", Map.of(
                                "lat", r.getUbicacionLat(),
                                "lng", r.getUbicacionLng(),
                                "actualizado", r.getUbicacionActualizado().toString()
                        )
                ))
                .toList();

        return ResponseEntity.ok(Map.of("repartidores", ubicaciones));
    }

    // GET /repartidores
    @GetMapping("/repartidores")
    public ResponseEntity<?> getRepartidores(HttpServletRequest request) {
        String rol = (String) request.getAttribute("rol");
        if (!"admin".equals(rol))
            return ResponseEntity.status(403).body(Map.of("error", "Solo admins"));

        List<Map<String, Object>> result = repartidorRepo.findAll().stream()
                .map(r -> {
                    Map<String, Object> m = new java.util.HashMap<>();
                    m.put("id", r.getId());
                    m.put("nombre", r.getNombre());
                    m.put("email", r.getEmail());
                    m.put("rol", r.getRol());
                    m.put("camionId", r.getCamion() != null ? r.getCamion().getId() : null);
                    m.put("ubicacionLat", r.getUbicacionLat());
                    m.put("ubicacionLng", r.getUbicacionLng());
                    m.put("ubicacionActualizado", r.getUbicacionActualizado());
                    m.put("createdAt", r.getCreatedAt());
                    return m;
                })
                .toList();

        return ResponseEntity.ok(result);
    }

    // PUT /repartidores/{id}
    @PutMapping("/repartidores/{id}")
    public ResponseEntity<?> updateRepartidor(@PathVariable Long id,
                                              @RequestBody Map<String, Object> body,
                                              HttpServletRequest request) {
        String rol = (String) request.getAttribute("rol");
        if (!"admin".equals(rol))
            return ResponseEntity.status(403).body(Map.of("error", "Solo admins"));

        Optional<Repartidor> opt = repartidorRepo.findById(id);
        if (opt.isEmpty())
            return ResponseEntity.status(404).body(Map.of("error", "Repartidor no encontrado"));

        Repartidor r = opt.get();
        if (body.containsKey("nombre")) r.setNombre((String) body.get("nombre"));
        if (body.containsKey("email"))  r.setEmail((String) body.get("email"));
        if (body.containsKey("camionId") && body.get("camionId") != null) {
            Long camionId = Long.valueOf(body.get("camionId").toString());
            camionRepo.findById(camionId).ifPresent(r::setCamion);
        }
        repartidorRepo.save(r);

        return ResponseEntity.ok(Map.of("mensaje", "Repartidor actualizado"));
    }
}
