package com.batoilogic.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
public class Producto {

    @Id
    private Long id; // ProductID del CSV (no autogenerado, viene del CSV)

    @Column(nullable = false, length = 200)
    private String nombre; // ProductName del CSV

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL)
    private List<TarifaProveedor> tarifas;
}
