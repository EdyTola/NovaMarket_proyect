# Actividad Práctica: Login y Productos con Angular 21

Fecha de referencia: 2026-05-17

## Objetivo

Integrar un frontend Angular 21 con una arquitectura de microservicios usando:

- Login contra el microservicio `auth`.
- JWT guardado en el frontend.
- Interceptor HTTP para enviar el token.
- Guard para proteger rutas.
- Consumo protegido del microservicio `producto`.

## Alcance de Esta Práctica

Esta práctica no implementa OAuth2/OIDC todavía.

El flujo usado es:

```text
Formulario Angular
  -> POST /auth/login
  -> JWT propio emitido por services/auth
  -> localStorage
  -> Authorization: Bearer <token>
```

El backend `services/auth-ms` usa Spring Security y JJWT para emitir tokens. Eso es suficiente para esta etapa didáctica, pero no equivale a Keycloak ni a un Authorization Server OAuth2.

La futura migración a Keycloak debe concentrarse principalmente en:

```text
src/app/core/auth/auth.service.ts
src/app/core/auth/auth.interceptor.ts
src/app/core/auth/auth.guard.ts
```

## Contexto

Los microservicios están registrados en Eureka:

```text
AUTH      auth-ms:8042
CATALOGO  catalogo-ms:8082
GATEWAY   gateway:8090
PRODUCTO  producto-ms:9092
```

Angular no consume directamente `auth-ms:8042` ni `producto-ms:9092`.
El frontend consume endpoints del gateway local. Para verificar que el gateway esta levantado:

```text
http://localhost:18080/actuator/health
```

## Contrato del Backend

### Login

Endpoint:

```text
POST /auth/login
```

Body:

```json
{
  "username": "usuario",
  "password": "password"
}
```

Respuesta esperada:

```json
{
  "accessToken": "...",
  "tokenType": "Bearer",
  "expiresIn": 3600000,
  "username": "usuario",
  "roles": ["ROLE_ADMIN"]
}
```

### Productos

Endpoint protegido por gateway y por el microservicio `producto`:

```text
GET /api/v1/productos
```

Debe enviarse:

```text
Authorization: Bearer <accessToken>
```

### Categorías

Endpoint público en esta etapa:

```text
GET /api/v1/categorias
```

En el gateway está permitido con:

```java
.pathMatchers("/api/v1/categorias/**", "/api/v1/ordenes/**", "/api/v1/pagos/**").permitAll()
```

Por eso `/categorias` no lleva `authGuard` en Angular. La ruta protegida en esta práctica es `/productos`.

## Actividad

### 1. Configurar la URL del gateway

Archivo:

```text
src/environments/environment.ts
```

Contenido:

```ts
export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8090'
};
```

### 2. Crear un servicio base para URLs

Archivo:

```text
src/app/core/services/api.service.ts
```

Responsabilidad:

- Centralizar la URL base.
- Evitar escribir `http://localhost:8090` en cada componente.

### 3. Crear `AuthService`

Archivo:

```text
src/app/core/auth/auth.service.ts
```

Responsabilidad:

- Enviar usuario y contraseña a `/auth/login`.
- Guardar el `accessToken`.
- Exponer `isAuthenticated()`.
- Exponer `logout()`.
- Guardar usuario y roles para mostrarlos en pantalla.

### 4. Crear el interceptor

Archivo:

```text
src/app/core/auth/auth.interceptor.ts
```

Responsabilidad:

- Leer el token desde `AuthService`.
- Agregar el header:

```text
Authorization: Bearer <token>
```

No debe agregar token al endpoint `/auth/login`.

### 5. Registrar HTTP e interceptor

Archivo:

```text
src/app/app.config.ts
```

Configuración esperada:

```ts
provideHttpClient(withInterceptors([authInterceptor]))
```

### 6. Crear el guard

Archivo:

```text
src/app/core/auth/auth.guard.ts
```

Responsabilidad:

- Permitir acceso si hay token.
- Redirigir a `/auth` si no hay sesión.
- Guardar `returnUrl` para volver a la ruta solicitada.

### 7. Configurar rutas

Archivo:

```text
src/app/app.routes.ts
```

Rutas esperadas:

```text
/auth
/categorias
/productos
```

La ruta `/productos` debe estar protegida con `authGuard`.

### 8. Implementar pantalla de login

Archivos:

```text
src/app/auth/auth.ts
src/app/auth/auth.html
```

La pantalla debe tener:

- Campo usuario.
- Campo contraseña.
- Botón ingresar.
- Mensaje simple si falla el login.

### 9. Implementar listado de productos

Archivos:

```text
src/app/productos/productos.service.ts
src/app/productos/productos.ts
src/app/productos/productos.html
```

La pantalla debe:

- Cargar productos desde `/api/v1/productos`.
- Mostrar tabla con `id`, `nombre`, `descripcion`, `idCategoria`.
- Permitir recargar la lista.

## Flujo Esperado

1. El alumno abre `/productos`.
2. Angular detecta que no hay token.
3. El guard redirige a `/auth`.
4. El alumno inicia sesión.
5. Angular guarda el token.
6. Angular vuelve a `/productos`.
7. El interceptor agrega el JWT.
8. El gateway autoriza la petición.
9. Se muestra el listado de productos.

## Checklist de Entrega

- `/auth` muestra formulario de login.
- Login exitoso guarda token.
- Header muestra usuario autenticado.
- Botón `Salir` elimina sesión.
- `/productos` no permite entrar sin login.
- `/productos` carga datos usando JWT.
- Las llamadas pasan por `http://localhost:8090`.

## Preguntas para el Alumno

1. ¿Por qué Angular consume el gateway y no el microservicio directo?
2. ¿Qué hace el interceptor?
3. ¿Qué problema resuelve el guard?
4. ¿Dónde se guarda temporalmente el JWT?
5. ¿Qué pasaría si el token no se envía al endpoint de productos?

## Siguiente Mejora

Mejoras sugeridas:

- Mostrar botones de crear, editar y eliminar solo si el usuario tiene rol `ROLE_ADMIN`.
- Validar también en backend, no solo en Angular.
- Mejorar validaciones visuales de formularios.
- Mostrar mensajes de error más específicos.
- Agregar pruebas unitarias para servicios y guards.
- Preparar una segunda práctica con Keycloak como proveedor OAuth2/OIDC.
- Configurar cliente SPA con Authorization Code + PKCE cuando se use Keycloak.
- Mantener autorización real en backend y usar Angular solo para mejorar la experiencia visual.
