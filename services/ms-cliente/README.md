# ms-cliente

Clientes del minimarket (`/api/v1/clientes`).

## DEV

```bash
cd services/ms-cliente
docker compose -f compose-dev.yml up -d
mvn spring-boot:run
```

- Eureka: **ms-cliente**
- Config: `http://localhost:18888/ms-cliente/dev`
