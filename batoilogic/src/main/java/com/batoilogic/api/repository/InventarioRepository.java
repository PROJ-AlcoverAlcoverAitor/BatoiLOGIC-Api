package com.batoilogic.api.repository;

import com.batoilogic.api.entity.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface InventarioRepository extends JpaRepository<Inventario, Long> {
    Optional<Inventario> findByProductoId(Long productoId);
}
