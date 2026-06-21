# NovaMarket — Minimarket POS (Microservicios)

Plataforma de minimarket con arquitectura de microservicios: Spring Boot, Spring Cloud, PostgreSQL, Kafka, observabilidad, Keycloak y Angular.

## Stack

Java 17 · Spring Boot 3.5 · Spring Cloud · PostgreSQL 16 · Kafka · Prometheus · Loki · Grafana · Keycloak · Angular 21

Guía detallada paso a paso: **[DEV.md](DEV.md)** · Mapa del negocio: **[MINIMARKET.md](MINIMARKET.md)**

---

## Cómo levantar todo el sistema (DEV)

### 0. Requisitos

- Java 17, Maven, Node.js/npm, Docker Desktop

### 1. Redes Docker (una sola vez)

```powershell
docker network create ecom-prod-net
docker network create ecom-dev-net
```

### 2. Infraestructura base (3 terminales Maven)

```powershell
cd infra/config-server   ; mvn spring-boot:run   # :18888
cd infra/registry-server ; mvn spring-boot:run   # :18761
cd infra/gateway         ; mvn spring-boot:run   # :18080
```

Comprobar: http://localhost:18080/actuator/health

### 3. PostgreSQL de cada microservicio (Docker)

```powershell
cd services/ms-auth      ; docker compose -f compose-dev.yml up -d   # :15431
cd services/ms-rubro     ; docker compose -f compose-dev.yml up -d   # :15432
cd services/ms-articulo  ; docker compose -f compose-dev.yml up -d   # :15433
cd services/ms-venta     ; docker compose -f compose-dev.yml up -d   # :15434
cd services/ms-pago      ; docker compose -f compose-dev.yml up -d   # :15435
cd services/ms-cliente   ; docker compose -f compose-dev.yml up -d   # :15436
```

### 4. Kafka (opcional)

```powershell
cd kafka ; docker compose -f compose-dev.yml up -d   # UI :41085, broker :41092
```

### 5. Keycloak (opcional — identidad OIDC, no reemplaza ms-auth aún)

```powershell
cd keycloak ; .\start-dev.ps1   # Admin :41880/admin (admin/admin), realm novamarket
```

### 6. Observabilidad (opcional)

```powershell
cd obs ; docker compose -f compose-dev.yml up -d   # Grafana :13000 (admin/admin)
```

### 7. Microservicios (1 terminal cada uno)

```powershell
cd services/ms-auth      ; mvn spring-boot:run
cd services/ms-rubro     ; mvn spring-boot:run
cd services/ms-articulo  ; mvn spring-boot:run
cd services/ms-cliente   ; mvn spring-boot:run
cd services/ms-venta     ; mvn spring-boot:run
cd services/ms-pago      ; mvn spring-boot:run
```

En Eureka (http://localhost:18761): **MS-AUTH**, **MS-RUBRO**, **MS-ARTICULO**, **MS-CLIENTE**, **MS-VENTA**, **MS-PAGO**.

### 8. Frontend Angular

```powershell
cd clients/ecom-ng
npm install
ng serve
```

http://localhost:4200 — login POS: `cajero` / `cajero123`

### Orden mínimo para probar caja

1. Infra (config + eureka + gateway)  
2. Postgres: ms-auth, ms-rubro, ms-articulo, ms-venta, ms-pago  
3. Maven: ms-auth, ms-rubro, ms-articulo, ms-venta, ms-pago  
4. Angular  

Crear al menos un **rubro** y un **artículo** con stock antes de usar **Caja**.

---

## Inicio rápido (PROD) — Docker

```powershell
docker network create ecom-prod-net

cd infra && docker compose up -d --build
# Config :28888 | Eureka :28761 | Gateway :28082

cd services/ms-auth      && docker compose up -d --build
cd services/ms-rubro     && docker compose up -d --build
cd services/ms-articulo  && docker compose up -d --build
cd services/ms-cliente   && docker compose up -d --build
cd services/ms-venta     && docker compose up -d --build
cd services/ms-pago      && docker compose up -d --build

cd kafka     && docker compose up -d
cd keycloak  && docker compose up -d --build
cd obs       && docker compose up -d
```

Modo mixto (Maven + infra Docker):

```powershell
$env:CONFIG_SERVER_URL="http://localhost:28888"
# eureka en config-repo: http://localhost:28761/eureka
```

---

## Puertos principales

| Componente | DEV | PROD |
|---|---:|---:|
| Gateway | 18080 | 28082 |
| Config Server | 18888 | 28888 |
| Eureka | 18761 | 28761 |
| Angular | 4200 | — |
| Keycloak | 41880 | 28180 |
| Kafka UI | 41085 | 28085 |
| Grafana | 13000 | 23000 |
| Microservicios | vía Gateway | vía Gateway |

---

## Estructura

```
NovaMarket/
├── infra/         config-server, registry-server, gateway, config-repo
├── services/      ms-auth, ms-rubro, ms-articulo, ms-cliente, ms-venta, ms-pago
├── clients/       ecom-ng (Angular POS)
├── kafka/         broker + UI
├── keycloak/      identidad OIDC (realm novamarket)
├── obs/           Prometheus, Loki, Grafana
└── docs/          libro digital (MkDocs)
```

## Documentación

Libro digital: [`docs/`](docs/) — `docs/compose.yml` sirve en :8002
