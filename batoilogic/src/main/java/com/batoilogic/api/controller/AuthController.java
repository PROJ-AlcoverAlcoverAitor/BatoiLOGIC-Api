package com.batoilogic.api.controller;

import com.batoilogic.api.dto.Dtos.*;
import com.batoilogic.api.entity.*;
import com.batoilogic.api.repository.*;
import com.batoilogic.api.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
public class AuthController {

    private final AdminRepository adminRepo;
    private final RepartidorRepository repartidorRepo;
    private final ClienteRepository clienteRepo;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AdminRepository adminRepo,
                          RepartidorRepository repartidorRepo,
                          ClienteRepository clienteRepo,
                          JwtUtil jwtUtil,
                          PasswordEncoder passwordEncoder) {
        this.adminRepo = adminRepo;
        this.repartidorRepo = repartidorRepo;
        this.clienteRepo = clienteRepo;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    // --- SETUP ---
    @PostMapping("/setup")
    public ResponseEntity<?> setup(@RequestParam String password,
                                   @RequestParam(defaultValue = "Admin") String nombre,
                                   @RequestParam(defaultValue = "admin@batoilogic.com") String email) {
        if (adminRepo.count() > 0) {
            return ResponseEntity.status(403).body(Map.of("error", "Setup ya realizado"));
        }

        Admin admin = new Admin();
        admin.setNombre(nombre);
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        adminRepo.save(admin);

        return ResponseEntity.status(201).body(Map.of("mensaje", "Admin creado correctamente", "email", email));
    }

    // --- LOGIN ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password) {
        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Faltan datos"));
        }

        // Buscar en admins
        Optional<Admin> admin = adminRepo.findByEmail(email);
        if (admin.isPresent()) {
            if (!passwordEncoder.matches(password, admin.get().getPassword())) {
                return ResponseEntity.status(401).body(Map.of("error", "Contraseña incorrecta"));
            }
            String token = jwtUtil.generarToken(admin.get().getId(), email, "admin");
            return ResponseEntity.ok(new LoginResponse(admin.get().getId(), admin.get().getNombre(), email, "admin", token));
        }

        // Buscar en repartidores
        Optional<Repartidor> repartidor = repartidorRepo.findByEmail(email);
        if (repartidor.isPresent()) {
            if (!passwordEncoder.matches(password, repartidor.get().getPassword())) {
                return ResponseEntity.status(401).body(Map.of("error", "Contraseña incorrecta"));
            }
            String token = jwtUtil.generarToken(repartidor.get().getId(), email, "empleado");
            return ResponseEntity.ok(new LoginResponse(repartidor.get().getId(), repartidor.get().getNombre(), email, "empleado", token));
        }

        // Buscar en clientes
        Optional<Cliente> cliente = clienteRepo.findByEmail(email);
        if (cliente.isPresent()) {
            if (!passwordEncoder.matches(password, cliente.get().getPassword())) {
                return ResponseEntity.status(401).body(Map.of("error", "Contraseña incorrecta"));
            }
            String token = jwtUtil.generarToken(cliente.get().getId(), email, "cliente");
            return ResponseEntity.ok(new LoginResponse(cliente.get().getId(), cliente.get().getNombre(), email, "cliente", token));
        }

        return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));
    }

    // --- LOGOUT ---
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String token = (String) request.getAttribute("token");
        if (token != null) jwtUtil.revocarToken(token);
        return ResponseEntity.ok(Map.of("mensaje", "Sesión cerrada correctamente"));
    }

    // --- REFRESH ---
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request) {
        String token = (String) request.getAttribute("token");
        Long userId = (Long) request.getAttribute("userId");
        String rol = (String) request.getAttribute("rol");
        String email = (String) request.getAttribute("email");

        jwtUtil.revocarToken(token);
        String nuevoToken = jwtUtil.generarToken(userId, email, rol);
        return ResponseEntity.ok(Map.of("token", nuevoToken));
    }

    // --- REGISTER ---
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestParam String nombre,
                                      @RequestParam String email,
                                      @RequestParam String password,
                                      @RequestParam(defaultValue = "cliente") String rol,
                                      HttpServletRequest request) {
        if (nombre == null || email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Faltan datos"));
        }

        // Solo admin puede crear empleados/admins
        if (rol.equals("empleado") || rol.equals("admin")) {
            String rolSolicitante = (String) request.getAttribute("rol");
            if (!"admin".equals(rolSolicitante)) {
                return ResponseEntity.status(403).body(Map.of("error", "Solo un admin puede crear empleados"));
            }
        }

        // Comprobar email duplicado
        if (adminRepo.findByEmail(email).isPresent() ||
            repartidorRepo.findByEmail(email).isPresent() ||
            clienteRepo.findByEmail(email).isPresent()) {
            return ResponseEntity.status(409).body(Map.of("error", "El email ya está registrado"));
        }

        String hashed = passwordEncoder.encode(password);

        switch (rol) {
            case "admin" -> {
                Admin a = new Admin();
                a.setNombre(nombre); a.setEmail(email); a.setPassword(hashed);
                adminRepo.save(a);
                return ResponseEntity.status(201).body(Map.of("id", a.getId(), "nombre", nombre, "email", email, "rol", rol));
            }
            case "empleado" -> {
                Repartidor r = new Repartidor();
                r.setNombre(nombre); r.setEmail(email); r.setPassword(hashed);
                repartidorRepo.save(r);
                return ResponseEntity.status(201).body(Map.of("id", r.getId(), "nombre", nombre, "email", email, "rol", rol));
            }
            default -> {
                Cliente c = new Cliente();
                c.setNombre(nombre); c.setEmail(email); c.setPassword(hashed);
                clienteRepo.save(c);
                return ResponseEntity.status(201).body(Map.of("id", c.getId(), "nombre", nombre, "email", email, "rol", rol));
            }
        }
    }
}
