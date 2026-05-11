package com.batoilogic.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "repartidores")
@Data
@NoArgsConstructor
public class Repartidor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 20)
    private String rol = "empleado";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camion_id")
    private Camion camion;

    @Column(name = "ubicacion_lat")
    private Double ubicacionLat;

    @Column(name = "ubicacion_lng")
    private Double ubicacionLng;

    @Column(name = "ubicacion_actualizado")
    private LocalDateTime ubicacionActualizado;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
