package com.batoilogic.api.repository;

import com.batoilogic.api.entity.Municipio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MunicipioRepository extends JpaRepository<Municipio, String> {
    List<Municipio> findByNomContainingIgnoreCase(String nom);
}