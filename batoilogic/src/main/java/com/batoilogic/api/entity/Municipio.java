package com.batoilogic.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "municipios")
@Data
@NoArgsConstructor
public class Municipio {

    @Id
    @Column(length = 10)
    private String codi; // Ej: "010014"

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(name = "codi_provincia", nullable = false, length = 5)
    private String codiProvincia; // Ej: "01"
}
