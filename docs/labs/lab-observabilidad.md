# Rubrica de Evaluacion - Observabilidad del Proyecto Final

Esta rubrica evalua que cada equipo pueda demostrar observabilidad real sobre los microservicios de su proyecto final.

No basta con levantar Grafana. El equipo debe demostrar que puede:

- saber si sus servicios estan vivos
- medir trafico y errores
- revisar logs centralizados
- configurar alertas utiles
- investigar una falla usando metricas y logs

---

## Alcance esperado

La evaluacion aplica a:

- `gateway` o punto de entrada principal
- cada microservicio de negocio del proyecto final
- servicios de apoyo que sean criticos para el flujo principal, si aplica
- componentes de seguridad o autenticacion, si forman parte del flujo

Cada equipo debe adaptar los nombres segun su proyecto.

Ejemplo:

| Tipo | Nombre en el proyecto | Debe observarse |
|---|---|---|
| Entrada | `gateway` | si |
| Microservicio 1 | `producto` | si |
| Microservicio 2 | `catalogo` | si |
| Microservicio 3 | `pedido` | si |
| Microservicio 4 | `pago` | si |

Regla:

```text
Todo microservicio que forma parte del flujo principal del proyecto debe tener metricas, logs y al menos una forma de detectar falla.
```

### Consideracion sobre Auth y Gateway

Si el proyecto ya tiene autenticacion y autorizacion integradas, se espera que el equipo observe tambien el comportamiento de seguridad.

Arquitectura aceptada para esta evaluacion:

```text
Cliente -> Gateway protegido -> Microservicios internos
```

En este escenario, es valido que `producto`, `catalogo` u otros microservicios internos no validen directamente el token, siempre que el equipo pueda explicar que:

- la proteccion de rutas se realiza en el `gateway`
- los microservicios internos no deberian exponerse publicamente
- el acceso externo debe entrar por el `gateway`
- los logs y metricas permiten ver intentos autorizados y no autorizados

Si un microservicio interno queda expuesto directamente al exterior sin validacion de seguridad, el equipo debe reconocerlo como riesgo arquitectonico.

---

## Evidencia minima a presentar

Cada equipo debe mostrar durante la sustentacion:

1. Prometheus con los servicios `UP`.
2. Dashboard en Grafana con metricas principales.
3. Loki con logs centralizados por servicio.
4. Al menos una alerta configurada y explicada.
5. Una falla controlada y su investigacion.
6. Explicacion clara de que problema detectaron las metricas y que causa mostraron los logs.
7. Evidencia de acceso protegido por `gateway`, si el proyecto ya integra Auth.

---

## Rubrica general

Nota total: **20 puntos**

Nota minima aprobatoria: **13 puntos**

| Criterio | Puntaje |
|---|---:|
| Metricas por microservicio | 5 |
| Logs centralizados por microservicio | 4 |
| Dashboard de observabilidad | 3 |
| Alertas | 3 |
| Caso de investigacion de falla | 3 |
| Explicacion tecnica durante la sustentacion | 2 |

---

## 1. Metricas por microservicio - 5 puntos

El equipo debe demostrar que Prometheus recolecta metricas de cada microservicio.

| Nivel | Descripcion | Puntaje |
|---|---|---:|
| Excelente | Todos los microservicios del proyecto aparecen en Prometheus como `UP` y exponen metricas HTTP, errores y recursos basicos. | 5 |
| Bueno | Todos los microservicios aparecen `UP`, pero faltan algunas metricas complementarias. | 4 |
| Basico | Solo algunos microservicios aparecen en Prometheus. | 2.5 |
| Insuficiente | Prometheus esta levantado, pero no recolecta metricas reales de los microservicios. | 1 |
| No presenta | No hay evidencia de Prometheus. | 0 |

Evidencia esperada:

```promql
up
```

```promql
sum by (job) (rate(http_server_requests_seconds_count[1m]))
```

```promql
sum by (job, status) (rate(http_server_requests_seconds_count{status=~"5.."}[1m]))
```

Opcional recomendado:

```promql
sum by (job) (jvm_memory_used_bytes)
```

```promql
avg by (job) (system_cpu_usage)
```

Que debe explicar el alumno:

- que significa `UP`
- que servicio esta recibiendo trafico
- que servicio esta generando errores
- diferencia entre disponibilidad, trafico y error
- como se observa una peticion autorizada y una no autorizada en el `gateway`

Metricas utiles cuando hay Auth:

```promql
sum by (job, status) (rate(http_server_requests_seconds_count{status=~"401|403"}[1m]))
```

Esta consulta permite observar respuestas no autorizadas (`401`) o prohibidas (`403`) cuando el `gateway` protege rutas.

---

## 2. Logs centralizados por microservicio - 4 puntos

El equipo debe demostrar que Loki recibe logs de cada microservicio.

| Nivel | Descripcion | Puntaje |
|---|---|---:|
| Excelente | Todos los microservicios tienen logs en Loki, separados por etiqueta `service`, y se puede seguir una peticion entre servicios. | 4 |
| Bueno | Todos los microservicios tienen logs, pero falta claridad para relacionar una peticion completa. | 3.2 |
| Basico | Solo algunos microservicios tienen logs centralizados. | 2 |
| Insuficiente | Loki esta levantado, pero no se demuestra consulta util por servicio. | 1 |
| No presenta | No hay evidencia de Loki. | 0 |

Evidencia esperada:

```logql
{service=~"gateway|servicio1|servicio2"}
```

Consulta por cada microservicio:

```logql
{service="nombre-del-servicio"}
```

Filtro por texto, si aplica:

```logql
{service="nombre-del-servicio"} |= "texto-importante"
```

Importante:

- no usar filtros demasiado especificos si no existen en el log
- primero probar solo por `service`
- luego filtrar por texto

Que debe explicar el alumno:

- que problema resuelve Loki
- por que ya no necesita abrir varias consolas
- como filtra logs por servicio
- que mensaje del log ayuda a explicar una falla
- que log evidencia una peticion rechazada por seguridad en el `gateway`
- que log evidencia una peticion autorizada que si llego al microservicio interno

---

## 3. Dashboard de observabilidad - 3 puntos

El dashboard debe permitir entender rapidamente el estado del sistema.

| Nivel | Descripcion | Puntaje |
|---|---|---:|
| Excelente | Dashboard claro con disponibilidad, trafico, errores y recursos por microservicio. | 3 |
| Bueno | Dashboard funcional con disponibilidad, trafico y errores. | 2.4 |
| Basico | Dashboard con una o dos metricas utiles. | 1.6 |
| Insuficiente | Dashboard creado, pero no ayuda a diagnosticar el sistema. | 0.8 |
| No presenta | No hay dashboard. | 0 |

Paneles minimos recomendados:

| Panel | Consulta sugerida | Objetivo |
|---|---|---|
| Servicios vivos | `up` | ver disponibilidad |
| Requests por segundo | `sum by (job) (rate(http_server_requests_seconds_count[1m]))` | ver trafico |
| Errores 5xx | `sum by (job, status) (rate(http_server_requests_seconds_count{status=~"5.."}[1m]))` | detectar fallas HTTP |
| Respuestas 401/403 | `sum by (job, status) (rate(http_server_requests_seconds_count{status=~"401|403"}[1m]))` | ver rechazos de seguridad |
| Memoria JVM | `sum by (job) (jvm_memory_used_bytes)` | ver consumo de memoria |
| CPU | `avg by (job) (system_cpu_usage)` | ver consumo de CPU |

Que debe explicar el alumno:

- que panel miraria primero ante un problema
- como identifica que servicio esta fallando
- como diferencia trafico normal de errores
- como identifica errores de autenticacion o autorizacion

---

## 4. Alertas - 3 puntos

El equipo debe configurar alertas que tengan sentido para su proyecto.

| Nivel | Descripcion | Puntaje |
|---|---|---:|
| Excelente | Tiene alertas por disponibilidad y errores, configuradas para servicios criticos, con mensaje claro. | 3 |
| Bueno | Tiene al menos dos alertas utiles. | 2.4 |
| Basico | Tiene una alerta simple por servicio caido. | 1.6 |
| Insuficiente | La alerta existe, pero no se puede explicar o no funciona. | 0.8 |
| No presenta | No hay alertas. | 0 |

Alertas minimas recomendadas:

### Servicio caido

```promql
up{job="nombre-del-servicio"} < 1
```

Uso:

```text
Detectar que Prometheus ya no puede consultar el microservicio.
```

### Errores HTTP 5xx

```promql
sum by (job) (rate(http_server_requests_seconds_count{status=~"5.."}[1m])) > 0
```

Uso:

```text
Detectar que el servicio responde con errores.
```

### Rechazos de seguridad 401/403

```promql
sum by (job, status) (rate(http_server_requests_seconds_count{job="nombre-del-gateway",status=~"401|403"}[1m])) > 0
```

Uso:

```text
Detectar intentos no autorizados o prohibidos en el gateway.
```

Ejemplo si el gateway se llama `gateway-dev`:

```promql
sum by (job, status) (rate(http_server_requests_seconds_count{job="gateway-dev",status=~"401|403"}[1m])) > 0
```

### Trafico anormalmente bajo

```promql
sum by (job) (rate(http_server_requests_seconds_count[5m])) == 0
```

Uso:

```text
Detectar que un servicio que normalmente recibe trafico dejo de recibirlo.
```

Que debe explicar el alumno:

- que condicion dispara la alerta
- que servicio afecta
- que haria al recibir esa alerta
- por que esa alerta es importante para su negocio
- si la alerta corresponde a disponibilidad, error de negocio o seguridad

---

## 5. Caso de investigacion de falla - 3 puntos

El equipo debe provocar o simular una falla controlada y explicar como la investigaria.

| Nivel | Descripcion | Puntaje |
|---|---|---:|
| Excelente | Provoca una falla, muestra impacto en metricas, revisa logs y explica causa probable. | 3 |
| Bueno | Muestra falla en metricas y logs, pero la explicacion es parcial. | 2.4 |
| Basico | Solo muestra que algo falla, sin conectar metricas y logs. | 1.6 |
| Insuficiente | La falla no se puede reproducir o no se observa claramente. | 0.8 |
| No presenta | No hay caso de falla. | 0 |

Ejemplos de fallas aceptadas:

- detener un microservicio de negocio
- detener una dependencia necesaria
- provocar una respuesta HTTP 500
- provocar un timeout entre servicios
- enviar una peticion invalida que genere error controlado
- llamar una ruta protegida sin token y observar `401`
- llamar una ruta protegida con token sin permisos y observar `403`, si aplica

Secuencia esperada:

1. Mostrar el sistema funcionando.
2. Generar trafico normal.
3. Provocar la falla.
4. Mostrar metricas afectadas.
5. Mostrar logs relacionados.
6. Explicar causa probable.
7. Restaurar el servicio o explicar como se restauraria.

Si la falla es de seguridad:

1. Mostrar una peticion valida con token.
2. Mostrar una peticion sin token o con token invalido.
3. Mostrar el `401` o `403` en metricas.
4. Mostrar el log del `gateway`.
5. Explicar por que `producto`, `catalogo` u otros microservicios internos no validan directamente el token.
6. Explicar por que esos microservicios no deben quedar expuestos fuera del `gateway`.

Que debe explicar el alumno:

```text
Las metricas me dijeron que algo paso.
Los logs me ayudaron a entender por que paso.
La alerta me aviso que debia mirar.
```

---

## 6. Explicacion tecnica durante la sustentacion - 2 puntos

Se evalua que el equipo entienda lo que muestra, no solo que lo tenga configurado.

| Nivel | Descripcion | Puntaje |
|---|---|---:|
| Excelente | Explica con claridad Prometheus, Loki, Grafana, metricas, logs y alertas usando su propio proyecto. | 2 |
| Bueno | Explica la mayoria de conceptos con algunos vacios menores. | 1.6 |
| Basico | Identifica herramientas, pero le cuesta explicar para que sirven. | 1 |
| Insuficiente | Solo muestra pantallas sin explicar su significado. | 0.4 |
| No presenta | No sustenta. | 0 |

Preguntas que el docente puede hacer:

1. Que te dice Prometheus que no te dice Loki?
2. Que te dice Loki que no te dice Prometheus?
3. Que significa que un servicio este `UP`?
4. Como sabes que un microservicio esta recibiendo trafico?
5. Como detectas errores 5xx?
6. Que alerta configuraste y por que?
7. Si un cliente reporta error, que miras primero?
8. Que log demuestra la causa probable de la falla?
9. Como sabes que el problema esta en un microservicio y no en otro?
10. Que mejorarias de tu observabilidad si tuvieras mas tiempo?
11. Donde se valida la seguridad: en gateway, en cada microservicio o en ambos?
12. Si `producto` o `catalogo` no validan token, por que no deberian exponerse directamente?
13. Como observarias intentos no autorizados?

---

## Matriz de revision por microservicio

Cada equipo debe completar una fila por cada microservicio del proyecto.

| Microservicio | `UP` en Prometheus | Requests visibles | Errores visibles | Logs en Loki | Alerta definida | Evidencia de falla | Seguridad observada |
|---|---|---|---|---|---|---|---|
| `gateway` | si/no | si/no | si/no | si/no | si/no | si/no | 401/403 o rutas protegidas |
| `servicio-1` | si/no | si/no | si/no | si/no | si/no | si/no | interno / valida token / no aplica |
| `servicio-2` | si/no | si/no | si/no | si/no | si/no | si/no | interno / valida token / no aplica |
| `servicio-3` | si/no | si/no | si/no | si/no | si/no | si/no | interno / valida token / no aplica |

Regla de evaluacion:

```text
Si un microservicio participa en el flujo principal, debe aparecer en esta matriz.
```

Para seguridad:

```text
Si la seguridad esta centralizada en gateway, debe evidenciarse en gateway.
Si un microservicio interno no valida token, debe justificarse como servicio interno no expuesto.
```

---

## Entregables sugeridos

El equipo debe entregar o mostrar:

- captura de Prometheus `targets`
- captura del dashboard de Grafana
- capturas de consultas Loki por servicio
- captura o descripcion de alertas configuradas
- breve explicacion del caso de falla
- matriz de revision por microservicio completada
- evidencia de una ruta protegida por gateway, si el proyecto ya tiene Auth

Formato recomendado:

```text
1. Nombre del proyecto
2. Lista de microservicios
3. Flujo principal evaluado
4. Evidencia de metricas
5. Evidencia de logs
6. Evidencia de alertas
7. Falla controlada
8. Evidencia de seguridad en gateway, si aplica
9. Conclusion del equipo
```

---

## Criterios de descuento

Se puede descontar puntaje si:

- solo se observa un microservicio y el proyecto tiene varios
- las consultas no corresponden al proyecto del equipo
- las capturas no muestran fecha, rango o datasource claro
- se usa dashboard sin explicar que representa
- se muestran logs sin relacionarlos con una peticion o falla
- las alertas existen pero no estan asociadas a un riesgo real
- el equipo no puede explicar que haria ante una alerta
- el equipo dice que la seguridad esta en gateway, pero expone microservicios internos directamente sin reconocer el riesgo
- no hay evidencia observable de respuestas `401` o `403` cuando el proyecto ya integra Auth

---

## Nivel esperado para aprobar

Para aprobar la parte de observabilidad, el equipo debe demostrar como minimo:

- todos sus microservicios principales aparecen en Prometheus
- al menos dos metricas utiles por microservicio
- logs centralizados en Loki por microservicio
- un dashboard basico en Grafana
- una alerta funcional
- una falla controlada explicada con metricas y logs
- evidencia de seguridad observada en gateway, si el proyecto ya integra Auth

Nota minima aprobatoria:

```text
13 / 20
```

---

## Frase guia para la sustentacion

```text
Nuestro sistema esta observable porque podemos ver si cada microservicio esta vivo,
cuanto trafico recibe, si esta generando errores, que dicen sus logs
y que alerta se dispara cuando algo importante falla.
```
