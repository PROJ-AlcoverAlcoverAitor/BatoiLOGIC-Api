package com.batoilogic.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "proveedores")
@Data
@NoArgsConstructor
public class Proveedor {

    @Id
    private Long id; // SupplierID del CSV (no autogenerado, viene del CSV)

    @Column(nullable = false, length = 150)
    private String nombre; // SupplierName del CSV

    @OneToMany(mappedBy = "proveedor", cascade = CascadeType.ALL)
    private List<TarifaProveedor> tarifas;
}
