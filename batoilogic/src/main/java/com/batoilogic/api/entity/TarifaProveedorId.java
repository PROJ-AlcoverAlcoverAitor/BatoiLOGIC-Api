package com.batoilogic.api.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
public class TarifaProveedorId implements Serializable {
    private Long productoId;
    private Long proveedorId;
}
