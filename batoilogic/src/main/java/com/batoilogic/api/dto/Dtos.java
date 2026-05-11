package com.batoilogic.api.dto;

import lombok.Data;

public class Dtos {

    @Data
    public static class LoginRequest {
        private String email;
        private String password;
    }

    @Data
    public static class LoginResponse {
        private Long id;
        private String nombre;
        private String email;
        private String rol;
        private String token;

        public LoginResponse(Long id, String nombre, String email, String rol, String token) {
            this.id = id;
            this.nombre = nombre;
            this.email = email;
            this.rol = rol;
            this.token = token;
        }
    }

    @Data
    public static class RegisterRequest {
        private String nombre;
        private String email;
        private String password;
        private String rol = "cliente";
    }

    @Data
    public static class UbicacionRequest {
        private Double lat;
        private Double lng;
    }

    @Data
    public static class EstadoRequest {
        private String estado;
    }

    @Data
    public static class IncidenciaRequest {
        private String incidencia;
    }

    @Data
    public static class SetupRequest {
        private String nombre = "Admin";
        private String email = "admin@batoilogic.com";
        private String password;
    }
}
