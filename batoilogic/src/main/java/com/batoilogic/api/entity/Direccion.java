package com.batoilogic.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

    @ManyToOne()
    @JoinColumn(name = "cliente_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Cliente cliente;

    @ManyToOne()
    @JoinColumn(name = "municipio_codi", nullable = false)
    private Municipio municipio;

    @Column(nullable = false, length = 255)
    private String calle;

    @Column(length = 10)
    private String numero;

    @Column(length = 10)
    private String piso;

    @Column(name = "es_principal")
    private Boolean esPrincipal = false;
}