package com.batoilogic.api.repository;

import com.batoilogic.api.entity.LinPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface LinPedidoRepository extends JpaRepository<LinPedido, Long> {
    List<LinPedido> findByPedidoId(Long pedidoId);
}
