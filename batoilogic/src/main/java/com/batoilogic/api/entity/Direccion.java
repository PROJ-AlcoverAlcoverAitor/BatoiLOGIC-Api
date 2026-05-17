package com.batoilogic.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "direcciones")
@Data
@NoArgsConstructor
public class Direccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cp_id", nullable = false)
    private CodigoPostal codigoPostal;

    @Column(nullable = false, length = 255)
    private String calle;

    @Column(length = 10)
    private String numero;

    @Column(length = 10)
    private String piso;

    @Column(name = "es_principal")
    private Boolean esPrincipal = false;
}
