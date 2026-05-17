package com.batoilogic.api.repository;

import com.batoilogic.api.entity.PedidoProv;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface PedidoProvRepository extends JpaRepository<PedidoProv, Long> {
    List<PedidoProv> findByEstado(String estado);
    List<PedidoProv> findByProveedorId(Long proveedorId);
}
