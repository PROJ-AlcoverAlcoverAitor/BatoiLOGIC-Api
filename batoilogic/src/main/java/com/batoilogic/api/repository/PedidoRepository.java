package com.batoilogic.api.repository;

import com.batoilogic.api.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByFecha(LocalDate fecha);
    List<Pedido> findByFechaAndRepartidorId(LocalDate fecha, Long repartidorId);
    List<Pedido> findByClienteId(Long clienteId);
}
