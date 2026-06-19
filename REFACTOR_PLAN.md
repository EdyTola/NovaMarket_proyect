# Memoria de Refactorización: `ecom` (ex-ProyectosMS2026)

> **Fecha:** 2026-05-25
> **Objetivo:** Migrar de MySQL a PostgreSQL, renombrar artefactos/contenedores con prefijo `ecom-` y sufijo `-ms`, estandarizar puertos, centralizar documentación.

---

## Resumen de cambios

| Fase | Cambio principal |
|---|---|
| **1. pom.xml** | `artifactId` → `ecom-*-ms`, MySQL→PostgreSQL driver |
| **2. Flyway SQL** | Sintaxis PostgreSQL (`AUTO_INCREMENT`→`IDENTITY`, `ON UPDATE` removido) |
| **3. config-repo** | JDBC `postgresql://`, driver `org.postgresql.Driver`, credenciales `ecom`/`ecom` |
| **4. Docker Compose** | MySQL→postgres:16-alpine, naming `ecom-*` en containers/images/volumes/networks |
| **5. .env** | Variables genéricas `DB_HOST`/`DB_PORT`/`DB_NAME`/`DB_USER`/`DB_PASS` |
| **6. Puertos** | Dev `server.port: 0` (dinámico), Prod `server.port: 8080`; Config:8099, Eureka:8761, Gateway:8090 |
| **7. Renombrar carpetas infra** | `config/`→`config/`, `eureka/`→`eureka/`, `config-repo/` movido a `config/config-repo/` |
| **8. Renombrar servicios** | `auth/`→`auth-ms/`, `catalogo/`→`catalogo-ms/`, `producto/`→`producto-ms/` |
| **9. `spring.application.name`** | `auth-ms`, `catalogo-ms`, `producto-ms` |
| **10. Gateway routes** | `lb://catalogo`→`lb://catalogo-ms`, etc. |
| **11. Feign client** | `@FeignClient(name = "catalogo-ms")` |
| **12. Config-repo filenames** | `auth-dev.yml`→`auth-ms-dev.yml`, etc. |
| **13. Redes separadas** | `ecom-prod-net` (prod), `ecom-dev-net` (kafka/obs dev) |
| **14. Docker compose naming** | `compose.yml` y `compose-dev.yml` (sin prefijo `docker-`) |
| **15. Documentación** | Centralizada en `docs/` con sesiones, labs y diagramas |

## Puertos

| Servicio | Puerto host PROD | Puerto container |
|---|---|---:|---:|
| config | 8888 | 8888 |
| eureka | 8761 | 8761 |
| gateway | 8090 | 8080 |
| auth-ms | 8042 | 8080 |
| catalogo-ms | 8082 | 8080 |
| producto-ms | 9092 | 8080 |
| orden-ms | 29051 | 8080 |
| pago-ms | 29061 | 8080 |

## PostgreSQL dev ports

| Servicio | Puerto |
|---|---:|
| auth-ms | 5401 |
| catalogo-ms | 5404 |
| producto-ms | 5407 |
| orden-ms | 5405 |
| pago-ms | 5406 |

Credenciales dev: `ecom` / `ecom`

## Naming

| Elemento | Antes | Después |
|---|---|---|
| artifactId | `ecom-auth`, `ecom-catalogo` | `ecom-auth-ms`, `ecom-catalogo-ms` |
| spring.application.name | `auth`, `catalogo`, `producto` | `auth-ms`, `catalogo-ms`, `producto-ms` |
| container_name compose | `ecom-auth`, `ecom-catalogo` | `ecom-auth-ms`, `ecom-catalogo-ms` |
| image | `ecom/auth:latest` | `ecom/auth-ms:latest` |
| Red externa | `ecom-prod-net` | `ecom-prod-net`, `ecom-dev-net` |
| Red interna | `catalogo-int` | `ecom-catalogo-int` |
| Config repo path | ruta absoluta Windows | Maven: `file:./config-repo`; Docker: `file:/config-repo` |
| Carpetas infra | `config/`, `eureka/` | `config/`, `eureka/` |
| Carpetas servicios | `auth/`, `catalogo/`, `producto/` | `auth-ms/`, `catalogo-ms/`, `producto-ms/` |

## Documentación

Centralizada en `docs/`:

```
docs/
├── index.md
├── sesiones/       ← s00-infraestructura, s04-gateway, s05-observabilidad, s06-seguridad, s07/s08-kafka
├── labs/           ← laboratorios y rúbricas
└── diagrams/       ← diagramas Mermaid (arquitectura, eureka, gateway, observabilidad, kafka)
```

Cada microservicio tiene un `README.md` conciso con qué hace, cómo ejecutarlo y vars de entorno clave.

## Para levantar

```bash
# Redes (una sola vez)
docker network create ecom-prod-net
docker network create ecom-dev-net

# Infraestructura
cd infra && docker compose up -d

# Servicios (cada service/*-ms/)
docker compose up -d

# Kafka
cd ../kafka && docker compose up -d

# Observabilidad
cd ../obs && docker compose up -d
```
