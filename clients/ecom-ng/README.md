# ecom-ng

Frontend Angular del proyecto ecom, construido con Angular 21 standalone para consumir microservicios mediante gateway.

## Inicio rápido

```bash
# Requisitos: Node 22+, infra + servicios levantados (ver README.md raíz)

cd clients/ecom-ng
npm install
ng serve
```

**Abrir:** http://localhost:4200

El frontend apunta a `http://localhost:18080` (Gateway). Para verificar que el gateway esta levantado usa `http://localhost:18080/actuator/health`.

---

## Estado Actual

La aplicación ya tiene una primera integración funcional con:

- Rutas standalone.
- Consumo HTTP mediante `provideHttpClient()`.
- Gateway centralizado en `environment.ts`.
- Login contra el microservicio `auth`.
- Persistencia básica del token JWT en `localStorage`.
- Interceptor HTTP para enviar `Authorization: Bearer <token>`.
- Guard para proteger la ruta `/productos`.
- CRUD de productos desde el microservicio `producto`.
- CRUD de categorías como práctica base, estandarizado con servicio propio.
- Selector de categorías en el formulario de productos.

## Modelo de Autenticación Actual

Esta etapa usa un modelo simple y didáctico:

```text
Angular /auth
  -> POST http://localhost:18080/auth/login
  -> recibe accessToken
  -> guarda JWT en localStorage
  -> consume endpoints protegidos con Bearer token
```

Importante: el microservicio `auth` actual no es un servidor OAuth2/OIDC. Es un servicio propio con Spring Security que valida usuario/contraseña y genera un JWT.

Librerías relevantes del backend actual:

```text
spring-boot-starter-security
jjwt-api
jjwt-impl
jjwt-jackson
```

Para una etapa futura con OAuth2/OIDC se recomienda usar Keycloak. En ese caso Angular ya no mostraría este formulario propio como flujo principal, sino que redirigiría al proveedor de identidad y volvería a la SPA mediante Authorization Code + PKCE.

## Backend Esperado

Antes de probar el frontend, deben estar levantados los servicios necesarios:

| Servicio | Puerto directo | Uso desde Angular |
| --- | ---: | --- |
| Gateway | `18080` | Sí |
| Auth | vía Gateway | No directo |
| Producto | vía Gateway | No directo |
| Catalogo | vía Eureka/Gateway | No directo |

Health del gateway:

```text
http://localhost:18080/actuator/health
```

Endpoints usados:

```text
POST http://localhost:18080/auth/login
GET  http://localhost:18080/api/v1/productos
GET  http://localhost:18080/api/v1/categorias
```

## Ejecutar

Desde la carpeta del proyecto:

```powershell
cd clients/ecom-ng
ng serve
```

Abrir:

```text
http://localhost:4200
```

También se puede usar:

```powershell
npm start
```

porque en `package.json` el script `start` ejecuta `ng serve`.

## Flujo de Prueba

1. Entrar a `/productos`.
2. La app redirige a `/auth` si no hay sesión.
3. Iniciar sesión con un usuario válido del microservicio `auth`.
4. Angular guarda el `accessToken`.
5. Volver a `/productos`.
6. El interceptor agrega el JWT al request.
7. El gateway permite consumir `/api/v1/productos`.

## Archivos Clave

| Archivo | Responsabilidad |
| --- | --- |
| `src/environments/environment.ts` | URL base del gateway |
| `src/app/core/services/api.service.ts` | Construcción de URLs hacia el gateway |
| `src/app/core/auth/auth.service.ts` | Login, sesión, token, roles y logout |
| `src/app/core/auth/auth.interceptor.ts` | Agrega el header `Authorization` |
| `src/app/core/auth/auth.guard.ts` | Protege rutas que requieren login |
| `src/app/auth/auth.ts` | Pantalla de login |
| `src/app/productos/productos.service.ts` | Consumo de productos |
| `src/app/productos/productos.ts` | Listado de productos |
| `src/app/app.routes.ts` | Rutas públicas y protegidas |
| `src/app/app.config.ts` | Providers globales de router y HTTP |

## Rutas (minimarket POS)

```text
/            Redirige a /pos
/auth        Login (vuelve a /pos tras login)
/pos         Caja: escáner, carrito, finalizar venta
/productos   CRUD productos (precio, stock, código barras)
/inventario  Alertas de stock bajo
/categorias  CRUD categorías
```

Endpoints adicionales vía gateway:

```text
GET  /api/v1/productos/codigo/{codigo}
GET  /api/v1/productos/alertas/stock-bajo
GET  /api/v1/clientes
POST /api/v1/ventas
```

## Validación Manual

Comandos sugeridos cuando quieras verificar:

```powershell
npm run build
npm test -- --watch=false
```

## Material de Práctica

- `ACTIVIDAD_AUTH_PRODUCTOS.md`: login, JWT, guard, interceptor y CRUD de productos.
- `ACTIVIDAD_CATEGORIAS_ESTANDAR.md`: refactor de categorías al estándar de servicios.
