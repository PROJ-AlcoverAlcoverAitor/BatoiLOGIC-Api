package com.batoilogic.api.service;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ProcesoExportacionYAsignacion {

    public static void main(String[] args) {
        System.out.println("Iniciado de manera independiente en el Sistema Operativo.");

        // Recoge la fecha enviada por el Daemon (hoy). Si se ejecuta a mano sin argumentos, usa la fecha actual.
        String fechaArgumento = (args.length > 0) ? args[0] : LocalDate.now().toString();
        LocalDate hoy = LocalDate.parse(fechaArgumento);
        LocalDate ayer = hoy.minusDays(1);

        System.out.println("Procesando pedidos de ayer (" + ayer + ") para moverlos a hoy (" + hoy + ")...");

        String urlDb = "jdbc:postgresql://localhost:5432/postgres";
        String userDb = "postgres";
        String passDb = "4206";

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(urlDb, userDb, passDb);
            conn.setAutoCommit(false); // Gestión manual de la transacción

            // 1. Obtener los IDs de los repartidores disponibles de la tabla 'repartidores'
            List<Long> idRepartidores = new ArrayList<>();
            String sqlRepartidores = "SELECT id FROM repartidores";

            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sqlRepartidores)) {
                while (rs.next()) {
                    idRepartidores.add(rs.getLong("id"));
                }
            }

            if (idRepartidores.isEmpty()) {
                System.err.println("Subproceso PSP ERROR: No se encontraron repartidores registrados en la tabla 'repartidores'.");
                return;
            }

            // 2. Preparación de consultas seguras con PreparedStatement (Evita SQL Injection)
            String sqlBuscarAyer = "SELECT id, cliente FROM pedidos WHERE fecha = ? AND estado = 'EN PREPARACIÓ'";
            String sqlModificarPedidos = "UPDATE pedidos SET estado = ?, fecha = ?, repartidor_id = ? WHERE id = ?";

            try (PreparedStatement pstmtBuscar = conn.prepareStatement(sqlBuscarAyer);
                 PreparedStatement pstmtModificar = conn.prepareStatement(sqlModificarPedidos)) {

                pstmtBuscar.setDate(1, Date.valueOf(ayer));
                ResultSet rsPedidos = pstmtBuscar.executeQuery();

                Random random = new Random();
                int contadorModificados = 0;

                while (rsPedidos.next()) {
                    long pedidoId = rsPedidos.getLong("id");
                    String cliente = rsPedidos.getString("cliente");

                    // Selección aleatoria de un ID de la lista de repartidores
                    long repartidorAleatorioId = idRepartidores.get(random.nextInt(idRepartidores.size()));

                    // Configuración de los nuevos valores para la comanda procesada
                    pstmtModificar.setString(1, "EN RUTA");
                    pstmtModificar.setDate(2, Date.valueOf(hoy));
                    pstmtModificar.setLong(3, repartidorAleatorioId);
                    pstmtModificar.setLong(4, pedidoId);

                    System.out.println("Exportando y reasignando Comanda ID: " + pedidoId + " de [" + cliente + "] al repartidor ID: " + repartidorAleatorioId);

                    pstmtModificar.executeUpdate();
                    contadorModificados++;
                }

                // Confirmación definitiva de los cambios en la base de datos
                conn.commit();
                System.out.println("Subproceso PSP EXITO: Automatizacion concluida. Se procesaron " + contadorModificados + " comandas.");
            }

        } catch (Exception e) {
            System.err.println("Subproceso PSP ERROR: Ocurrio un fallo en el procesamiento: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("Subproceso PSP: Rollback completado de forma segura ante fallos.");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}