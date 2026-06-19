# ms-pago

Registro de pagos: efectivo, tarjeta, Yape. API `POST /api/v1/pagos/registrar`.

## DEV

```bash
cd services/ms-pago
docker compose -f compose-dev.yml up -d
mvn spring-boot:run
```

- Eureka: **ms-pago**
- Config: `http://localhost:18888/ms-pago/dev`
