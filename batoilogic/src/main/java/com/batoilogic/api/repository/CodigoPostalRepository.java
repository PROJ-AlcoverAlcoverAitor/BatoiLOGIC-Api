package com.batoilogic.api.repository;

import com.batoilogic.api.entity.CodigoPostal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface CodigoPostalRepository extends JpaRepository<CodigoPostal, Long> {
    Optional<CodigoPostal> findByCodigo(String codigo);
}
