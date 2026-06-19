# ms-rubro

Microservicio de **rubros** (clasificación de artículos). Expone `/api/v1/categorias`.

## DEV

```bash
cd services/ms-rubro
docker compose -f compose-dev.yml up -d
mvn spring-boot:run
```

- Eureka: **ms-rubro**
- Config: `http://localhost:18888/ms-rubro/dev`
