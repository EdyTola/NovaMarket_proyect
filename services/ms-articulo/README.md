# ms-articulo

Microservicio de **artículos** (precio, stock, código de barras). Consume **ms-rubro** vía Feign.

## DEV

```bash
cd services/ms-articulo
docker compose -f compose-dev.yml up -d
mvn spring-boot:run
```

- Eureka: **ms-articulo**
- Config: `http://localhost:18888/ms-articulo/dev`
