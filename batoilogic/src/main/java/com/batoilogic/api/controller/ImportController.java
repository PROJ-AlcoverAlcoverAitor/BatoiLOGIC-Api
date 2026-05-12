package com.batoilogic.api.controller;

import com.batoilogic.api.entity.*;
import com.batoilogic.api.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/import")
public class ImportController {

    private final MunicipioRepository municipioRepo;
    private final ProveedorRepository proveedorRepo;
    private final ProductoRepository productoRepo;
    private final TarifaProveedorRepository tarifaRepo;

    public ImportController(MunicipioRepository municipioRepo,
                            ProveedorRepository proveedorRepo,
                            ProductoRepository productoRepo,
                            TarifaProveedorRepository tarifaRepo) {
        this.municipioRepo = municipioRepo;
        this.proveedorRepo = proveedorRepo;
        this.productoRepo = productoRepo;
        this.tarifaRepo = tarifaRepo;
    }

    // --- MUNICIPIOS ---
    // Recibe lista de objetos: [{codi, nom, codiProvincia}, ...]
    @PostMapping("/municipios")
    public ResponseEntity<?> importarMunicipios(@RequestBody List<Map<String, String>> datos,
                                                HttpServletRequest request) {
        String rol = (String) request.getAttribute("rol");
        if (!"admin".equals(rol)) {
            return ResponseEntity.status(403).body(Map.of("error", "Solo admin puede importar"));
        }
        int count = 0;
        for (Map<String, String> row : datos) {
            String codi = row.get("codi");
            if (municipioRepo.existsById(codi)) continue; // Evita duplicados

            Municipio m = new Municipio();
            m.setCodi(codi);
            m.setNom(row.get("nom"));
            m.setCodiProvincia(row.get("codiProvincia"));
            municipioRepo.save(m);
            count++;
        }
        return ResponseEntity.ok(Map.of("insertados", count));
    }

    // --- TARIFAS (incluye proveedores y productos) ---
    // Recibe lista de objetos: [{productId, productName, supplierId, supplierName, price}, ...]
    @PostMapping("/tarifas")
    public ResponseEntity<?> importarTarifas(@RequestBody List<Map<String, String>> datos,
                                             HttpServletRequest request) {
        String rol = (String) request.getAttribute("rol");
        if (!"admin".equals(rol)) {
            return ResponseEntity.status(403).body(Map.of("error", "Solo admin puede importar"));
        }

        int count = 0;
        for (Map<String, String> row : datos) {
            Long productId = Long.parseLong(row.get("productId"));
            Long supplierId = Long.parseLong(row.get("supplierId"));

            Producto producto = productoRepo.findById(productId).orElseGet(() -> {
                Producto p = new Producto();
                p.setId(productId);
                p.setNombre(row.get("productName"));
                return productoRepo.save(p);
            });

            Proveedor proveedor = proveedorRepo.findById(supplierId).orElseGet(() -> {
                Proveedor p = new Proveedor();
                p.setId(supplierId);
                p.setNombre(row.get("supplierName"));
                return proveedorRepo.save(p);
            });

            TarifaProveedorId tarifaId = new TarifaProveedorId();
            tarifaId.setProductoId(productId);
            tarifaId.setProveedorId(supplierId);

            if (tarifaRepo.existsById(tarifaId)) continue;

            TarifaProveedor tarifa = new TarifaProveedor();
            tarifa.setId(tarifaId);
            tarifa.setProducto(producto);
            tarifa.setProveedor(proveedor);
            tarifa.setPrecio(new BigDecimal(row.get("price")));
            tarifaRepo.save(tarifa);
            count++;
        }
        return ResponseEntity.ok(Map.of("insertados", count));
    }
}
