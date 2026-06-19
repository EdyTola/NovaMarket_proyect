# Snapshot pre-Kafka - Microservicios UPEU

Fecha de referencia: 2026-05-09, entorno America/Lima.

Este documento deja una foto rapida del estado actual del proyecto antes de iniciar la integracion de comunicacion asincrona con Kafka.

## Estado del workspace

- Carpeta revisada: `.`
- No se detecto repositorio Git en esta carpeta: `git status` devuelve `fatal: not a git repository`.
- No se hicieron cambios funcionales en codigo ni configuracion durante esta foto.
- No hay integracion Kafka activa en el codigo. La unica mencion encontrada es documental, en observabilidad.

## Modulos presentes

### Infraestructura

- `infra/config-server`: servidor de configuracion Spring Cloud Config.
- `infra/registry-server`: registro Eureka.
- `infra/gateway`: Spring Cloud Gateway con seguridad JWT reactiva.
- `infra/config-repo`: configuracion centralizada por servicio y perfil.
- `infra/compose.yml`: levanta config, eureka y gateway en la red `ecom-prod-net`.

### Servicios de negocio

- `services/auth`: autenticacion, usuarios, roles, generacion de JWT.
- `services/catalogo`: CRUD de categorias.
- `services/producto`: CRUD de productos, cliente Feign hacia catalogo, circuit breaker Resilience4j.

### Observabilidad

- `observability`: Prometheus, Grafana, Loki, Promtail y documentacion de la sesion de observabilidad.
- Los servicios exponen endpoints de actuator y prometheus segun configuracion.

## Seguridad actual

### Auth

- `services/auth` usa Spring Security con sesiones stateless.
- `/auth/login`, Swagger y endpoints basicos de actuator estan publicos.
- El resto requiere autenticacion.
- `JwtService` emite tokens firmados con secreto compartido y `issuer: auth`.
- Configuracion JWT en `infra/config-repo/auth-ms-dev.yml`.

### Gateway

- `infra/gateway` valida JWT como resource server reactivo.
- Permite health/info/prometheus, `/auth/**`, Swagger y preflight `OPTIONS`.
- El resto de rutas requiere autenticacion.
- Lee roles desde el claim `roles` sin prefijo adicional.
- Configuracion JWT en `infra/config-repo/gateway-dev.yml`.

### Producto

- `services/producto` valida JWT como resource server.
- Permite actuator, Swagger y `OPTIONS`.
- `POST`, `PUT` y `DELETE` de `/api/v1/productos/**` requieren `ROLE_ADMIN` via `hasRole("ADMIN")`.
- El resto requiere autenticacion.
- Configuracion JWT en `infra/config-repo/producto-dev.yml`.

### Catalogo

- `services/catalogo` actualmente no tiene seguridad propia en el codigo revisado.
- La proteccion de acceso queda principalmente en gateway.

## Comunicacion actual entre microservicios

- Comunicacion sincronica principal: `producto -> catalogo` mediante OpenFeign.
- `ProductoServiceImpl.findDetalleById` consulta categoria por id usando `CatalogoClient`.
- Resilience4j protege la llamada a catalogo con circuit breaker `catalogo`.
- Fallback actual: devuelve el producto con `categoria: null`.

## Configuracion y puertos dev

- Config Server: `7071` interno, `7072` publicado en compose de infra.
- Registry Server: `7081` local segun config dev, `7082` publicado en compose de infra.
- Gateway: `7091` interno, `7092` publicado en compose de infra.
- Auth dev: `8041`, MySQL dev `3341`.
- Catalogo dev: `8081`, MySQL dev `3381`.
- Producto dev: `9091`, MySQL dev `3391`.

## Dependencias relevantes actuales

- Spring Boot:
  - `producto` y `catalogo`: `3.5.12`
  - `auth`: `3.5.14`
- Spring Cloud:
  - `producto` y `catalogo`: `2025.0.1`
  - `auth`: `2025.0.2`
- Bases de datos: MySQL 8.4 en compose dev por servicio.
- Migraciones: Flyway presente, pero en dev aparece `flyway.enabled: false`.
- Observabilidad: actuator + prometheus.
- Seguridad:
  - `auth`: Spring Security + JJWT.
  - `producto` y `gateway`: OAuth2 resource server + JOSE/JWT.

## Punto de partida recomendado para Kafka

Para introducir Kafka sin mezclar demasiados cambios, conviene hacerlo por fases:

1. Agregar infraestructura Kafka en compose.
   - Broker Kafka.
   - Interfaz de administracion opcional, por ejemplo Kafka UI.
   - Variables comunes en config-repo.

2. Agregar dependencias `spring-kafka` solo en los servicios que publiquen o consuman eventos.
   - Candidato inicial productor: `producto`.
   - Candidato inicial consumidor: `catalogo` o un nuevo flujo dependiente del caso del curso.

3. Definir contratos de eventos.
   - Paquete sugerido: `event` o `messaging`.
   - Ejemplos posibles: `ProductoCreadoEvent`, `ProductoActualizadoEvent`, `ProductoEliminadoEvent`.
   - Mantener DTOs de API separados de eventos Kafka.

4. Centralizar nombres de topics en configuracion.
   - Ejemplo: `app.kafka.topics.producto-events`.
   - Evitar strings quemados en producers/listeners.

5. Mantener trazabilidad.
   - Propagar `X-Correlation-Id` como header Kafka.
   - Conservar logs con trace/correlation id para no perder lo logrado en observabilidad.

6. Probar por capas.
   - Primero levantar broker.
   - Luego producer simple.
   - Luego consumer.
   - Finalmente integrar flujo funcional.

## Observaciones pequenas antes de Kafka

- Al no haber Git en la carpeta actual, conviene inicializar repo o ubicar la raiz real del repo antes de cambios grandes.
- En `infra/config-repo/catalogo-dev.yml`, el bloque `devtools` parece quedar indentado bajo `eureka`; revisar antes de depender de esa configuracion.
- Las versiones de Spring Boot/Spring Cloud no estan totalmente alineadas entre servicios. No bloquea Kafka, pero vale la pena tenerlo presente.
- La seguridad de `catalogo` podria quedar solo por gateway; si se ejecuta directo en `8081`, no tendria la misma proteccion que producto.

## Proximo paso sugerido

Integrar Kafka empezando por infraestructura y configuracion comun, despues agregar un producer en `producto` para publicar eventos de cambios de producto, y finalmente crear el consumer que necesite el caso practico del curso.
