package com.batoilogic.api.repository;

import com.batoilogic.api.entity.Camion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CamionRepository extends JpaRepository<Camion, Long> {
    boolean existsByMatricula(String matricula);
}