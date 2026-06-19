# ecom — Plataforma de Microservicios 2026

Proyecto educativo de microservicios con Spring Boot, Spring Cloud, PostgreSQL, Kafka y observabilidad.

## Stack

Java 17, Spring Boot 3.5, Spring Cloud, PostgreSQL 16, Kafka, Prometheus, Loki, Grafana, Angular 21

---

Guía paso a paso DEV: **[DEV.md](DEV.md)**

## Inicio rápido (DEV) — Maven local

```bash
# 1. Redes Docker (una sola vez)
docker network create ecom-prod-net
docker network create ecom-dev-net

# 2. Infraestructura (Maven, cada uno en su terminal)
cd infra/config-server    && mvn spring-boot:run   # http://localhost:18888
cd infra/registry-server  && mvn spring-boot:run   # http://localhost:18761
cd infra/gateway          && mvn spring-boot:run   # http://localhost:18080/actuator/health

# 3. PostgreSQL para cada servicio que vayas a levantar
cd services/ms-auth      && docker compose -f compose-dev.yml up -d   # :15431
cd services/ms-rubro    && docker compose -f compose-dev.yml up -d   # :15432
cd services/ms-articulo && docker compose -f compose-dev.yml up -d   # :15433

# 4. Microservicios (cada uno en su terminal)
cd services/ms-auth      && mvn spring-boot:run
cd services/ms-rubro     && mvn spring-boot:run
cd services/ms-articulo  && mvn spring-boot:run
cd services/ms-cliente   && mvn spring-boot:run
cd services/ms-venta     && mvn spring-boot:run
cd services/ms-pago      && mvn spring-boot:run

# 5. Frontend
cd clients/ecom-ng && npm install && ng serve   # http://localhost:4200
```

## Inicio rápido (PROD) — Docker

```bash
# 1. Red
docker network create ecom-prod-net

# 2. Levantar infraestructura (Docker compila las imágenes)
cd infra && docker compose up -d --build
#   http://localhost:28888 — Config Server
#   http://localhost:28761  — Eureka Dashboard
#   http://localhost:28082/actuator/health  — API Gateway health

# 3. Servicios (Docker compila cada imagen)
cd ../services/ms-auth      && docker compose up -d --build
cd ../services/ms-rubro     && docker compose up -d --build --scale ms-rubro=3
cd ../services/ms-articulo  && docker compose up -d --build
cd ../services/ms-venta     && docker compose up -d --build
cd ../services/ms-pago      && docker compose up -d --build
```

Para modo mixto (Maven + Docker): arranca infra en Docker y pasa estas variables a tus servicios Maven:
```bash
CONFIG_SERVER_URL=http://localhost:28888
eureka.client.service-url.defaultZone=http://localhost:28761/eureka
```

## Puertos

| Componente | Puerto host DEV (Maven) | Puerto host PROD (Docker) |
|---|---:|---:|
| Config Server | 18888 | 28888 |
| Eureka | 18761 | 28761 |
| Gateway | 18080 | 28082 |
| ms-auth | dinámico | vía Gateway |
| ms-rubro | dinámico | vía Gateway |
| ms-articulo | dinámico | vía Gateway |
| ms-venta | dinámico | vía Gateway |
| ms-pago | dinámico | vía Gateway |
| ms-cliente | dinámico | vía Gateway |

## Estructura

```
ecom/
├── infra/         ← config-server, registry-server, gateway, config-repo
├── services/      ← ms-auth, ms-rubro, ms-articulo, ms-cliente, ms-venta, ms-pago
├── clients/       ← ecom-ng (Angular)
├── kafka/         ← broker + UI
├── obs/           ← Prometheus, Loki, Grafana
├── docs/          ← libro digital (MkDocs)
└── MINIMARKET.md  ← visión y mapa del minimarket POS
```

## Documentación

Libro digital: [`docs/`](docs/) (MkDocs, sirve en :8002 con `docs/compose.yml`)
