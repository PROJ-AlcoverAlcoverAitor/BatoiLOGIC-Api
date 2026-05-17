package com.batoilogic.api.repository;

import com.batoilogic.api.entity.Ciudad;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CiudadRepository extends JpaRepository<Ciudad, Long> {}
