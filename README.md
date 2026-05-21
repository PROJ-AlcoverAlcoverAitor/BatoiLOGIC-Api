# BatoiLogic API

API REST para el sistema de gestión de pedidos y repartidores de BatoiLogic. Desarrollada con Spring Boot + Hibernate + PostgreSQL.

## Requisitos

- Java 17+
- Maven
- PostgreSQL corriendo localmente
- IntelliJ IDEA (recomendado)

## Configuración

Edita `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/batoilogic
spring.datasource.username=batoiuser
spring.datasource.password=4206
server.port=5000
jwt.secret=TU_SECRET_KEY
```

## Arrancar

Desde IntelliJ: ejecuta `BatoiLogicApplication.java`

O desde terminal:
```bash
mvn spring-boot:run
```

## Setup inicial

Al arrancar por primera vez, crea el admin:
```bash
curl -X POST "http://localhost:5000/setup?password=TU_PASSWORD&nombre=Admin&email=admin@batoilogic.com"
```

## Documentación (Swagger UI)

```
http://localhost:5000/swagger-ui
```

## Endpoints

### Autenticación
| Método | Ruta | Descripción | Auth |
|--------|------|-------------|------|
| POST | `/setup` | Crear primer admin | No |
| POST | `/login` | Login (admin/empleado/cliente) | No |
| POST | `/logout` | Cerrar sesión | Token |
| POST | `/refresh` | Renovar token | Token |
| POST | `/register` | Registrar usuario | No (cliente) / Admin (empleado/admin) |

### Pedidos
| Método | Ruta | Descripción | Auth |
|--------|------|-------------|------|
| GET | `/pedidos?dia=` | Listar pedidos del día | Token |
| POST | `/pedidos` | Crear pedido | Admin/Empleado |
| GET | `/pedidos/{id}` | Detalle de pedido | Token |
| PATCH | `/pedidos/{id}/estado` | Actualizar estado | Token |
| PATCH | `/pedidos/{id}/incidencia` | Añadir incidencia | Token |

### Repartidores
| Método | Ruta | Descripción | Auth |
|--------|------|-------------|------|
| POST | `/repartidor/ubicacion` | Enviar ubicación GPS | Empleado |
| GET | `/repartidores/ubicaciones` | Ver flota en tiempo real | Admin |

### Importación
| Método | Ruta | Descripción | Auth |
|--------|------|-------------|------|
| POST | `/admin/import/municipios` | Importar municipios | Admin |
| POST | `/admin/import/tarifas` | Importar tarifas de proveedores | Admin |

## Roles

| Rol | Descripción |
|-----|-------------|
| `admin` | Acceso total |
| `empleado` | Solo sus pedidos asignados, puede actualizar estado/incidencia y enviar GPS |
| `cliente` | Acceso a app cliente (no al backoffice) |

## Estructura

```
src/main/java/com/batoilogic/api/
├── config/          # SecurityConfig
├── controller/      # AuthController, PedidoController, RepartidorController, ImportController
├── dto/             # DTOs de request/response
├── entity/          # Entidades JPA (Admin, Repartidor, Cliente, Pedido, Producto, Proveedor...)
├── repository/      # Repositorios Spring Data JPA
├── security/        # JwtUtil, JwtFilter
└── service/         # NominatimService (geocodificación)
```

## Base de datos

Tablas principales gestionadas por Hibernate (`ddl-auto=update`):

- `admins`, `repartidores`, `clientes`
- `pedidos`
- `camiones`
- `municipios`
- `productos`, `proveedores`, `tarifas_proveedor`

## Tecnologías

- Spring Boot 3.2.5
- Spring Security + JWT (jjwt 0.12.5)
- Spring Data JPA + Hibernate 6
- PostgreSQL
- Springdoc OpenAPI (Swagger UI)
- Lombok
- Nominatim (geocodificación gratuita)

## Migración pendiente

Este proyecto es la migración de una API Flask (Python) a Spring Boot. Pendiente:
- Integración con Odoo
- Módulo de rutas y asignación automática
- Servicio demonio para preparación diaria de pedidos a las 6:00
