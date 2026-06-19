# ms-auth

Autenticación JWT y roles (ADMIN, CAJERO, SUPERVISOR, REPARTIDOR).

## DEV

```bash
cd services/ms-auth
docker compose -f compose-dev.yml up -d
mvn spring-boot:run
```

- Eureka: **ms-auth**
- Login: `POST http://localhost:18080/auth/login`
- Usuarios seed: `cajero` / `cajero123`, `admin` / `admin123`
