# ecom — Plataforma de Microservicios 2026

Proyecto educativo de arquitectura distribuida con Spring Boot, Spring Cloud, PostgreSQL, Kafka y observabilidad.

## Estructura del proyecto

```
ecom/                      ← raíz del monorepositorio
├── infra/                 ← config-server, registry-server, gateway, config-repo
├── services/              ← microservicios backend (-ms)
├── clients/               ← frontends (-ng)
├── kafka/                 ← plataforma de eventos (EDA)
├── obs/                   ← observabilidad (Prometheus, Loki, Grafana)
└── docs/                  ← libro digital (MkDocs Material)
```

## Componentes

| Componente | Dentro de | Rol | Puerto host PROD | Puerto container |
|---|---|---|---:|---:|
| **Config Server** | `infra/config-server/` | Configuración centralizada | 28888 | 8888 |
| **Registry Server** | `infra/registry-server/` | Service discovery (Eureka) | 28761 | 8761 |
| **API Gateway** | `infra/gateway/` | Punto único de entrada + JWT | 28082 | 8080 |
| **auth-ms** | `services/auth-ms/` | Autenticación y emisión JWT | vía Gateway | 8080 |
| **catalogo-ms** | `services/catalogo-ms/` | Gestión de categorías | vía Gateway | 8080 |
| **producto-ms** | `services/producto-ms/` | Gestión de productos + Feign + CB | vía Gateway | 8080 |
| **orden-ms** | `services/orden-ms/` | Órdenes + Kafka producer | vía Gateway | 8080 |
| **pago-ms** | `services/pago-ms/` | Pagos + Kafka consumer | vía Gateway | 8080 |
| **ecom-ng** | `clients/ecom-ng/` | SPA Angular (frontend) | - | - |

> Los servicios backend usan `server.port: 0` (aleatorio) en desarrollo local y `server.port: 8080` en Docker.

## Stack

- Java 17, Spring Boot 3.5, Spring Cloud 2025
- PostgreSQL 16, Flyway
- Kafka (KRaft mode, sin Zookeeper)
- Prometheus, Loki, Grafana
- Docker Compose
- Angular 19 (cliente web)

## Inicio rápido

### DEV (Maven local)

```bash
# 1. Crear redes Docker (una sola vez, si no existen)
docker network create ecom-prod-net
docker network create ecom-dev-net

# 2. Infraestructura (Maven, cada uno en su terminal)
cd infra/config-server    && mvn spring-boot:run   # http://localhost:18888
cd infra/registry-server  && mvn spring-boot:run   # http://localhost:18761
cd infra/gateway            && mvn spring-boot:run   # http://localhost:18080/actuator/health

# 3. PostgreSQL para cada servicio
cd services/auth-ms     && docker compose -f compose-dev.yml up -d   # :15431
cd services/catalogo-ms && docker compose -f compose-dev.yml up -d   # :15432
cd services/producto-ms && docker compose -f compose-dev.yml up -d   # :15433

# 4. Microservicios (cada uno en su terminal)
cd services/auth-ms      && mvn spring-boot:run
cd services/catalogo-ms  && mvn spring-boot:run
cd services/producto-ms  && mvn spring-boot:run
```

### PROD (Docker)

```bash
# 1. Redes (una sola vez)
docker network create ecom-prod-net

# 2. Infraestructura (healthchecks: gateway espera a eureka, eureka a config)
cd infra && docker compose up -d --build
#   http://localhost:28888 — Config Server
#   http://localhost:28761  — Eureka Dashboard
#   http://localhost:28082/actuator/health  — API Gateway health

# 3. Servicios
cd services/auth-ms      && docker compose up -d
cd services/catalogo-ms  && docker compose up -d
cd services/producto-ms  && docker compose up -d
cd services/orden-ms     && docker compose up -d
cd services/pago-ms      && docker compose up -d
```

## Puertos de desarrollo (PostgreSQL)

| Servicio | DB | Puerto host dev | Puerto interno |
|---|---|---|---:|
| auth-ms | `ecom_auth_db` | 15431 | 5432 |
| catalogo-ms | `ecom_catalogo_db` | 15432 | 5432 |
| producto-ms | `ecom_producto_db` | 15433 | 5432 |
| orden-ms | `ecom_orden_db` | 15434 | 5432 |
| pago-ms | `ecom_pago_db` | 15435 | 5432 |

Credenciales: `ecom` / `ecom`
