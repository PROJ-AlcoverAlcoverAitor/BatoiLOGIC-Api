package com.batoilogic.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pedidos")
@Data
@NoArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "repartidor_id")
    private Long repartidorId;

    @Column(name = "cliente_id")
    private Long clienteId;

    @Column(nullable = false, length = 100)
    private String cliente;

    @Column(nullable = false, length = 255)
    private String direccion;

    @Column(nullable = false)
    private LocalDate fecha;

    // Estados según enunciado: EN PREPARACIÓ, PREPARAT, EN RUTA, ENTREGAT, NO ENTREGAT
    @Column(nullable = false, length = 50)
    private String estado = "EN PREPARACIÓ";

    @Column(nullable = false, length = 20)
    private String telefono;

    @Column(nullable = false)
    private Double lat;

    @Column(nullable = false)
    private Double lng;

    @Column(columnDefinition = "TEXT")
    private String incidencia;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
