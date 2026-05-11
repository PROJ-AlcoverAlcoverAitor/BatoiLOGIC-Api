package com.batoilogic.api.repository;

import com.batoilogic.api.entity.Repartidor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RepartidorRepository extends JpaRepository<Repartidor, Long> {
    Optional<Repartidor> findByEmail(String email);
}
