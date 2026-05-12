package com.batoilogic.api.repository;

import com.batoilogic.api.entity.TarifaProveedor;
import com.batoilogic.api.entity.TarifaProveedorId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TarifaProveedorRepository extends JpaRepository<TarifaProveedor, TarifaProveedorId> {}