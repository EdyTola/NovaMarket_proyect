# keycloak

Proveedor de identidad **OAuth2 / OpenID Connect** para NovaMarket. Carpeta independiente (mismo patron que `kafka/` y `obs/`).

Sustituye de forma futura a `ms-auth` como emisor de tokens. Hoy **no modifica** el gateway ni Angular: convive con el flujo actual (`POST /auth/login` + JWT HMAC).

## Servicios

| Servicio | Puerto host DEV | Puerto host PROD | Puerto container |
|---|---:|---:|---:|
| Keycloak | 41880 | 28180 | 8080 |
| PostgreSQL Keycloak | — | 25437 | 5432 |

## Realm importado: `novamarket`

| Elemento | Valor |
|---|---|
| Realm | `novamarket` |
| Consola admin | usuario `admin` / `admin` (bootstrap) |
| Cliente SPA | `ecom-ng` (public, PKCE, redirect `http://localhost:4200/*`) |
| Cliente referencia | `ecom-gateway` |
| Roles | `ROLE_ADMIN`, `ROLE_USER`, `ROLE_CAJERO`, `ROLE_SUPERVISOR`, `ROLE_REPARTIDOR` |

Usuarios demo (mismas credenciales que `ms-auth` / `DataInitializer`):

| Usuario | Password | Rol |
|---|---|---|
| admin | admin123 | ROLE_ADMIN |
| cajero | cajero123 | ROLE_CAJERO |
| user | user123 | ROLE_USER |
| supervisor | supervisor123 | ROLE_SUPERVISOR |
| repartidor | repartidor123 | ROLE_REPARTIDOR |

El mapper `realm-roles-claim` publica el claim **`roles`** en el access token, compatible con la configuracion actual del gateway (`authoritiesClaimName: roles`).

## Inicio rapido

### Red Docker (una sola vez)

```powershell
docker network create ecom-dev-net
docker network create ecom-prod-net
```

### DEV

```powershell
cd keycloak
.\start-dev.ps1
```

O manualmente:

```powershell
docker network create ecom-dev-net
cd keycloak
docker compose -f compose-dev.yml build keycloak
docker compose -f compose-dev.yml up -d
docker logs -f ecom-keycloak-dev
```

**Primer arranque:** `docker compose build` tarda ~10 min (una sola vez). Luego Keycloak importa el realm en ~2-5 min. Usa PostgreSQL (no H2) para evitar corrupcion en Windows.

Verificar:

```powershell
curl.exe -s -o NUL -w "realm:%{http_code}" http://localhost:41880/realms/novamarket
curl.exe -s -o NUL -w "admin:%{http_code}" http://localhost:41880/admin/
```

Debe devolver `realm:200` y `admin:302`.

Enlaces:

- Consola admin: http://localhost:41880/admin (admin / admin)
- Realm: http://localhost:41880/realms/novamarket
- OpenID config: http://localhost:41880/realms/novamarket/.well-known/openid-configuration

Desde contenedores en `ecom-dev-net`: `http://keycloak:8080`

Si falla o queda colgado:

```powershell
cd keycloak
docker compose -f compose-dev.yml down
docker compose -f compose-dev.yml up -d --build
docker logs -f ecom-keycloak-dev
```

Espera en logs: `Keycloak 25.0.6 ... started` e `Import finished successfully`

### PROD

```powershell
cd keycloak
copy .env.example .env
docker compose up -d --build
```

- Consola: http://localhost:28180/admin
- BD host: `localhost:25437` (solo si necesitas acceso externo a Postgres de Keycloak)

## Probar token (password grant — solo laboratorio)

```powershell
curl -s -X POST "http://localhost:41880/realms/novamarket/protocol/openid-connect/token" ^
  -H "Content-Type: application/x-www-form-urlencoded" ^
  -d "grant_type=password" ^
  -d "client_id=ecom-ng" ^
  -d "username=cajero" ^
  -d "password=cajero123"
```

Usar el `access_token` contra el gateway cuando este configurado con `issuer-uri` (ver `config/gateway-keycloak.example.yml`).

## Estructura

```text
keycloak/
├── compose-dev.yml          # Keycloak dev (H2 embebido, import realm)
├── compose.yml              # Keycloak prod + PostgreSQL 16
├── .env.example
├── realm/
│   └── novamarket-realm.json
├── config/
│   └── gateway-keycloak.example.yml
└── README.md
```

## Integracion con el resto del proyecto

```text
                    ┌─────────────────┐
  ecom-ng (4200) ──►│    Keycloak     │── JWT RS256
                    │  realm novamarket│
                    └────────┬────────┘
                             │ issuer-uri / JWKS
                    ┌────────▼────────┐
                    │  API Gateway    │──► ms-rubro, ms-articulo, ...
                    └─────────────────┘

  Flujo actual (convive):
  ecom-ng ──POST /auth/login──► ms-auth ── JWT HMAC ──► Gateway
```

Pasos para migrar (manual, cuando decidas):

1. Levantar `keycloak/` y verificar realm `novamarket`.
2. Cambiar `infra/gateway` a `spring.security.oauth2.resourceserver.jwt.issuer-uri` (ejemplo en `config/gateway-keycloak.example.yml`).
3. Angular: OIDC con cliente `ecom-ng` (Authorization Code + PKCE).
4. Opcional: dejar `ms-auth` solo para usuarios legacy o retirarlo.

## Operacion

Reimportar realm (solo si el volumen de datos es nuevo o se borra el contenedor):

- DEV: `docker compose -f compose-dev.yml down -v` y volver a `up -d` (dev usa almacenamiento efimero).
- PROD: el import corre al primer arranque; cambios en `novamarket-realm.json` requieren export/import o ajustes en consola.

Logs:

```powershell
docker logs -f ecom-keycloak-dev
```

Documentacion del curso: [`../docs/sesiones/s06-seguridad.md`](../docs/sesiones/s06-seguridad.md)
