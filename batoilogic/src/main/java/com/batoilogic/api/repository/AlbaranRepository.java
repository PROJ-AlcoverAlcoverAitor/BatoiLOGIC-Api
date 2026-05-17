package com.batoilogic.api.repository;

import com.batoilogic.api.entity.Albaran;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface AlbaranRepository extends JpaRepository<Albaran, Long> {
    Optional<Albaran> findByPedidoId(Long pedidoId);
}
