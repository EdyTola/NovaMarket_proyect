# Rubrica de Evaluacion - Proyecto Final de Microservicios

Nota total: **20 puntos**

Nota minima aprobatoria: **12 puntos**

---

## Resumen de puntajes

| Criterio | Puntaje maximo |
|---|---|
| Arquitectura y diseno | 3 |
| Implementacion de microservicios | 4 |
| Comunicacion entre servicios | 2 |
| Seguridad | 2 |
| Infraestructura y contenedores | 2 |
| Base de datos y persistencia | 1 |
| Kafka / mensajeria | 2 |
| Observabilidad | 2 |
| Informe escrito | 1 |
| Sustentacion oral | 1 |
| **Total** | **20** |

---

## 1. Arquitectura y Diseno - 3 puntos

### 1.1 Diagrama y justificacion de la arquitectura (1 pt)

| Nivel | Descripcion | Puntaje |
|---|---|---|
| Excelente | Diagrama completo con todos los componentes, relaciones claras, justificacion de decisiones arquitectonicas. | 1 |
| Bueno | Diagrama completo pero falta justificacion de algunas decisiones. | 0.8 |
| Basico | Diagrama incompleto o sin justificacion. | 0.5 |
| Insuficiente | Sin diagrama o no corresponde al proyecto. | 0 |

### 1.2 Division en microservicios (1 pt)

| Nivel | Descripcion | Puntaje |
|---|---|---|
| Excelente | Servicios con responsabilidades bien definidas, tama~no adecuado, sin acoplamiento innecesario. | 1 |
| Bueno | Servicios bien separados pero con algun solapamiento de responsabilidades. | 0.8 |
| Basico | Varios servicios con responsabilidades mezcladas o un monolito disfrazado de microservicios. | 0.5 |
| Insuficiente | No hay separacion clara o proyecto no aplica microservicios. | 0 |

### 1.3 Naming y estructura del repositorio (1 pt)

| Nivel | Descripcion | Puntaje |
|---|---|---|
| Excelente | Nombres consistentes (`-ms` para servicios, `-ng` para frontends), estructura de carpetas clara, compose.yml y compose-dev.yml separados. | 1 |
| Bueno | Estructura clara pero con algunas inconsistencias de nomenclatura. | 0.8 |
| Basico | Estructura funcional pero desordenada o sin convencion clara. | 0.5 |
| Insuficiente | Sin estructura definida, todo en la raiz. | 0 |

---

## 2. Implementacion de Microservicios - 4 puntos

### 2.1 Configuracion centralizada (1 pt)

| Nivel | Descripcion | Puntaje |
|---|---|---|
| Excelente | Todos los servicios usan config-server con perfiles dev y prod separados. | 1 |
| Bueno | Configuracion centralizada pero sin separacion de perfiles. | 0.8 |
| Basico | Algunos servicios usan config-server, otros no. | 0.5 |
| Insuficiente | Sin config-server, configuracion hardcodeada. | 0 |

### 2.2 Registro y descubrimiento (1 pt)

| Nivel | Descripcion | Puntaje |
|---|---|---|
| Excelente | Todos los servicios se registran en Eureka y se comunican por nombre, no por IP. | 1 |
| Bueno | Registro en Eureka pero con algunas rutas hardcodeadas. | 0.8 |
| Basico | Eureka levantado pero no todos los servicios se registran. | 0.5 |
| Insuficiente | Sin Eureka o sin registro de servicios. | 0 |

### 2.3 Gateway como punto de entrada (1 pt)

| Nivel | Descripcion | Puntaje |
|---|---|---|
| Excelente | Gateway con rutas hacia todos los servicios, balanceo de carga, validacion JWT o delegacion de auth. | 1 |
| Bueno | Gateway funcional pero sin balanceo o sin auth. | 0.8 |
| Basico | Gateway con rutas parciales. | 0.5 |
| Insuficiente | Sin gateway o acceso directo a servicios desde el exterior. | 0 |

### 2.4 API REST y buenas practicas (1 pt)

| Nivel | Descripcion | Puntaje |
|---|---|---|
| Excelente | Endpoints RESTful consistentes, uso correcto de verbos HTTP, codigos de estado, validacion de entrada. | 1 |
| Bueno | REST consistente pero faltan validaciones o codigos de estado adecuados. | 0.8 |
| Basico | Endpoints funcionales pero no siguen REST completamente. | 0.5 |
| Insuficiente | Endpoints sin estructura clara o solo GET/POST. | 0 |

---

## 3. Comunicacion entre Servicios - 2 puntos

### 3.1 Feign Client y resiliencia (1 pt)

| Nivel | Descripcion | Puntaje |
|---|---|---|
| Excelente | Comunicacion via Feign Client, con Circuit Breaker (Resilience4j) configurado y fallback implementado. | 1 |
| Bueno | Feign Client funcional pero sin Circuit Breaker o sin fallback. | 0.8 |
| Basico | Comunicacion via RestTemplate en lugar de Feign. | 0.5 |
| Insuficiente | Comunicacion directa por IP o sin cliente HTTP declarativo. | 0 |

### 3.2 Manejo de errores entre servicios (1 pt)

| Nivel | Descripcion | Puntaje |
|---|---|---|
| Excelente | Errores entre servicios manejados con timeouts, reintentos,断路器和 respuestas degradadas. | 1 |
| Bueno | Algun manejo de errores pero faltan timeouts o断路. | 0.8 |
| Basico | Errores manejados solo con try-catch basicos. | 0.5 |
| Insuficiente | Sin manejo de errores entre servicios, fallas en cascada. | 0 |

---

## 4. Seguridad - 2 puntos

### 4.1 Autenticacion (1 pt)

| Nivel | Descripcion | Puntaje |
|---|---|---|
| Excelente | JWT implementado con login, registro, vencimiento de token, y proteccion de rutas en gateway. | 1 |
| Bueno | JWT implementado pero sin vencimiento o sin proteccion completa de rutas. | 0.8 |
| Basico | Autenticacion basica (sin JWT) o solo en algunos servicios. | 0.5 |
| Insuficiente | Sin autenticacion o credenciales hardcodeadas. | 0 |

### 4.2 Autorizacion y buenas practicas (1 pt)

| Nivel | Descripcion | Puntaje |
|---|---|---|
| Excelente | Roles implementados, gateway valida token, servicios internos confian en gateway, secretos en variables de entorno. | 1 |
| Bueno | Roles implementados pero sin separacion clara de responsabilidades de seguridad. | 0.8 |
| Basico | Solo proteccion basica de rutas sin roles. | 0.5 |
| Insuficiente | Sin autorizacion o secretos hardcodeados en el codigo. | 0 |

---

## 5. Infraestructura y Contenedores - 2 puntos

### 5.1 Docker Compose (1 pt)

| Nivel | Descripcion | Puntaje |
|---|---|---|
| Excelente | Compose files separados (compose.yml para prod, compose-dev.yml para dev), con naming consistente, redes separadas. | 1 |
| Bueno | Compose funcional pero sin separacion dev/prod o sin redes logicas. | 0.8 |
| Basico | Un solo compose con todos los servicios. | 0.5 |
| Insuficiente | Sin Docker o compose incompleto. | 0 |

### 5.2 Variables de entorno y .env (1 pt)

| Nivel | Descripcion | Puntaje |
|---|---|---|
| Excelente | Variables de entorno genericas (DB_HOST, DB_PORT, etc.), `.env` y `.env.example` versionados, sin secretos hardcodeados. | 1 |
| Bueno | Variables de entorno pero sin `.env.example` o algunas hardcodeadas. | 0.8 |
| Basico | Solo algunas variables externalizadas. | 0.5 |
| Insuficiente | Todo hardcodeado o sin variables de entorno. | 0 |

---

## 6. Base de Datos y Persistencia - 1 punto

| Nivel | Descripcion | Puntaje |
|---|---|---|
| Excelente | Cada microservicio con su propia base de datos, migraciones Flyway, script SQL versionado. | 1 |
| Bueno | Base de datos por servicio pero sin migraciones. | 0.8 |
| Basico | Base de datos compartida entre servicios. | 0.5 |
| Insuficiente | Sin base de datos o solo datos en memoria. | 0 |

---

## 7. Kafka / Mensajeria - 2 puntos (si aplica)

Si el proyecto no usa Kafka, este puntaje se redistribuye:
- +1 a Implementacion de microservicios
- +1 a Observabilidad

### 7.1 Topicos y eventos (1 pt)

| Nivel | Descripcion | Puntaje |
|---|---|---|
| Excelente | Topicos bien nombrados, eventos con esquema claro, productores y consumidores implementados. | 1 |
| Bueno | Kafka funcional pero sin esquema de eventos claro. | 0.8 |
| Basico | Kafka configurado pero solo un productor o consumidor basico. | 0.5 |
| Insuficiente | Sin Kafka o no funcional. | 0 |

### 7.2 Integracion con el flujo del negocio (1 pt)

| Nivel | Descripcion | Puntaje |
|---|---|---|
| Excelente | Kafka integrado en el flujo principal del negocio, eventos disparan acciones reales en otros servicios. | 1 |
| Bueno | Kafka funcional pero en un flujo secundario o de prueba. | 0.8 |
| Basico | Kafka presente pero sin conexion con la logica de negocio. | 0.5 |
| Insuficiente | Sin integracion de Kafka. | 0 |

---

## 8. Observabilidad - 2 puntos

| Nivel | Descripcion | Puntaje |
|---|---|---|
| Excelente | Prometheus recolecta metricas de todos los servicios, Loki centraliza logs, Grafana dashboard, alertas configuradas, caso de falla demostrado. | 2 |
| Bueno | Metricas y logs configurados pero faltan alertas o caso de falla. | 1.6 |
| Basico | Solo Prometheus o solo Loki, sin dashboard integrado. | 1 |
| Insuficiente | Sin observabilidad o solo metricas basicas sin explicacion. | 0 |

Para la rubrica a detalle de observabilidad, ver `lab-observabilidad.md` (20 pts, escalable a 2).

---

## 9. Informe Escrito - 1 punto

| Nivel | Descripcion | Puntaje |
|---|---|---|
| Excelente | Informe completo siguiendo la plantilla, con diagramas, tablas, instrucciones claras de ejecucion. | 1 |
| Bueno | Informe completo pero faltan algunos detalles tecnicos. | 0.8 |
| Basico | Informe incompleto o sin diagramas. | 0.5 |
| Insuficiente | Sin informe o muy pobre. | 0 |

---

## 10. Sustentacion Oral - 1 punto

| Nivel | Descripcion | Puntaje |
|---|---|---|
| Excelente | Todos los integrantes participan, explican con claridad, responden preguntas tecnicas. | 1 |
| Bueno | Explicacion clara pero solo uno o dos integrantes participan. | 0.8 |
| Basico | Explicacion basica sin profundidad tecnica. | 0.5 |
| Insuficiente | No sustentan o no pueden explicar su propio proyecto. | 0 |

---

## Resumen de calificacion

| Criterio | Puntaje obtenido | Puntaje maximo |
|---|---|---|
| Arquitectura y diseno | | 3 |
| Implementacion de microservicios | | 4 |
| Comunicacion entre servicios | | 2 |
| Seguridad | | 2 |
| Infraestructura y contenedores | | 2 |
| Base de datos y persistencia | | 1 |
| Kafka / mensajeria | | 2 |
| Observabilidad | | 2 |
| Informe escrito | | 1 |
| Sustentacion oral | | 1 |
| **Total** | | **20** |

---

## Criterios de descuento generales

- Proyecto que no compila o no se levanta: -4 puntos
- Microservicios que se comunican por IP directa sin Eureka: -1 punto
- Secretos hardcodeados en el codigo: -1 punto
- `target/`, `node_modules/` u otros artefactos en el repositorio: -0.5 puntos
- Sin `.gitignore` o `.gitignore` insuficiente: -0.5 puntos
- Documentacion minima o inexistente: -1 punto
- Un solo integrante hace todo el trabajo: -2 puntos (evaluacion docente)

---

## Nota final

| Rango | Calificacion |
|---|---|
| 18 - 20 | Excelente |
| 15 - 17 | Bueno |
| 12 - 14 | Aprobado |
| 0 - 11 | Desaprobado |
