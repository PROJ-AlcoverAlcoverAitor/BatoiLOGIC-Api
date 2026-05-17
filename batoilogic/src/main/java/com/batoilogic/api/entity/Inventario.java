package com.batoilogic.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventario")
@Data
@NoArgsConstructor
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false, unique = true)
    private Producto producto;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(name = "stock_min", nullable = false)
    private Integer stockMin = 5;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
