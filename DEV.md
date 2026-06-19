# Arranque DEV — NovaMarket

## 1. Una sola vez

```powershell
docker network create ecom-prod-net
docker network create ecom-dev-net
```

## 2. Infraestructura (3 terminales Maven)

```powershell
cd infra/config-server   ; mvn spring-boot:run   # :18888
cd infra/registry-server ; mvn spring-boot:run   # :18761
cd infra/gateway         ; mvn spring-boot:run   # :18080
```

Comprobar: http://localhost:18080/actuator/health

## 3. PostgreSQL (Docker)

```powershell
cd services/ms-auth      ; docker compose -f compose-dev.yml up -d   # :15431
cd services/ms-rubro     ; docker compose -f compose-dev.yml up -d   # :15432
cd services/ms-articulo  ; docker compose -f compose-dev.yml up -d   # :15433
cd services/ms-venta     ; docker compose -f compose-dev.yml up -d   # :15434
cd services/ms-cliente   ; docker compose -f compose-dev.yml up -d   # :15436
cd services/ms-pago      ; docker compose -f compose-dev.yml up -d   # :15435
```

## 4. Kafka (opcional; ventas funcionan sin Kafka si el pago es síncrono)

```powershell
cd kafka ; docker compose -f compose-dev.yml up -d   # :41092
```

## 5. Microservicios (1 terminal cada uno)

```powershell
cd services/ms-auth      ; mvn spring-boot:run
cd services/ms-rubro     ; mvn spring-boot:run
cd services/ms-articulo  ; mvn spring-boot:run
cd services/ms-cliente   ; mvn spring-boot:run
cd services/ms-venta     ; mvn spring-boot:run
cd services/ms-pago      ; mvn spring-boot:run
```

En Eureka (http://localhost:18761) deben aparecer: **MS-AUTH**, **MS-RUBRO**, **MS-ARTICULO**, **MS-CLIENTE**, **MS-VENTA**, **MS-PAGO**.

## 6. Frontend

```powershell
cd clients/ecom-ng
npm install
ng serve
```

http://localhost:4200 — login: `cajero` / `cajero123`

## Orden mínimo para probar caja

1. Infra (config + eureka + gateway)  
2. Postgres: ms-auth, ms-rubro, ms-articulo, ms-venta, ms-pago  
3. Maven: ms-auth, ms-rubro, ms-articulo, ms-venta, ms-pago  
4. Angular  

Crear al menos un **rubro** y un **artículo** con código de barras y stock antes de usar **Caja**.
