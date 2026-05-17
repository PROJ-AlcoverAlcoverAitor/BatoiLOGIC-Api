package com.batoilogic.api.repository;

import com.batoilogic.api.entity.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
public interface RutaRepository extends JpaRepository<Ruta, Long> {
    List<Ruta> findByFecha(LocalDate fecha);
    List<Ruta> findByFechaAndRepartidorId(LocalDate fecha, Long repartidorId);
}
