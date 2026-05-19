package com.batoilogic.api.controller;

import com.batoilogic.api.entity.Municipio;
import com.batoilogic.api.repository.MunicipioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/municipios")
public class MunicipioController {

    @Autowired
    private MunicipioRepository municipioRepository;

    @GetMapping
    public ResponseEntity<List<Municipio>> listarTodos() {
        List<Municipio> lista = municipioRepository.findAll();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Municipio>> buscarMunicipios(@RequestParam String nombre) {
        // Usamos el método que creamos al principio en el repositorio
        List<Municipio> lista = municipioRepository.findByNomContainingIgnoreCase(nombre.trim());

        // Limitamos a 10 resultados para no colapsar la red ni la interfaz de C#
        List<Municipio> filtrada = lista.stream().limit(10).collect(Collectors.toList());

        return ResponseEntity.ok(filtrada);
    }
}