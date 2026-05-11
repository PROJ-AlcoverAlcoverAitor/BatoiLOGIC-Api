package com.batoilogic.api.controller;

import com.batoilogic.api.entity.Repartidor;
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

    public RepartidorController(RepartidorRepository repartidorRepo) {
        this.repartidorRepo = repartidorRepo;
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
}
