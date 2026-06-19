# ms-venta

Microservicio de **ventas POS** (`/api/v1/ventas`). Descuenta stock en **ms-articulo** y registra pago en **ms-pago**.

## DEV

```bash
cd services/ms-venta
docker compose -f compose-dev.yml up -d
mvn spring-boot:run
```

- Eureka: **ms-venta**
- Config: `http://localhost:18888/ms-venta/dev`
