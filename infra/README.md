# infra

Infraestructura base del sistema distribuido: Config Server, Registry Server (Eureka) y API Gateway.

## Estructura

```text
infra/
├── config-server/     ← Spring Cloud Config Server
├── config-repo/       ← configuración por servicio y perfil
├── registry-server/   ← Eureka (service discovery)
├── gateway/           ← Spring Cloud Gateway + JWT
└── compose.yml        ← stack Docker de producción
```

## Componentes

| Directorio | Puerto host DEV | Puerto host PROD | Puerto container | Rol |
|---|---:|---:|---:|---|
| `config-server/` | 18888 | 28888 | 8888 | Configuración centralizada |
| `registry-server/` | 18761 | 28761 | 8761 | Service discovery |
| `gateway/` | 18080 | 28082 | 8080 | Punto único de entrada HTTP + JWT |

---

## DEV (Maven local)

Levantar cada servicio en su propia terminal, en este orden:

```bash
cd config-server    && mvn spring-boot:run   # http://localhost:18888
cd ../registry-server && mvn spring-boot:run # http://localhost:18761
cd ../gateway       && mvn spring-boot:run   # http://localhost:18080/actuator/health
```

**Enlaces:**
- Config Server: http://localhost:18888/ms-rubro/dev
- Eureka Dashboard: http://localhost:18761
- Gateway health: http://localhost:18080/actuator/health

> Los microservicios en `services/*-ms` usan los mismos puertos en dev (18888, 18761, 18080).
> Docker Compose de infra usa puertos distintos en el host (28888, 28761, 28082) para no pisarlos.

---

## PROD (Docker)

```bash
docker compose up -d --build
```

Gateway espera a Registry Server; Registry espera a Config Server. Cada healthcheck usa `/actuator/health`.

**Enlaces:**
- Config Server: http://localhost:28888/catalogo-ms/prod
- Eureka Dashboard: http://localhost:28761
- Gateway health: http://localhost:28082/actuator/health

> `config-server` no se registra en Eureka. `registry-server` y `gateway` aparecen en el dashboard.

Gateway necesita `JWT_SECRET` en `infra/.env`. Debe coincidir con `services/auth-ms/.env`.

En Docker, los contenedores conservan alias `ecom-config` y `ecom-eureka` para compatibilidad con los `.env` de los microservicios.

Detalle del gateway: [`gateway/README.md`](gateway/README.md) (si existe).

---

Documentación completa en [`../docs/`](../docs/).
