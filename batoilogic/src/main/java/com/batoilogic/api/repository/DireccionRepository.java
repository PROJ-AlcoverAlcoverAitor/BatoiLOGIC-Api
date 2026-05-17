package com.batoilogic.api.repository;

import com.batoilogic.api.entity.Direccion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface DireccionRepository extends JpaRepository<Direccion, Long> {
    List<Direccion> findByClienteId(Long clienteId);
}
